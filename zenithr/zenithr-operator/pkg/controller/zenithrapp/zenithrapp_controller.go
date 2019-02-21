package zenithrapp

import (
	"context"
	"encoding/json"
	"fmt"
	. "github.com/kiegroup/zenithr-operator/pkg/controller/zenithrapp/constants"
	"k8s.io/apimachinery/pkg/util/intstr"
	"reflect"
	"time"

	zenithrv1 "github.com/kiegroup/zenithr-operator/pkg/apis/zenithr/v1"
	knative "github.com/knative/serving/pkg/apis/serving/v1alpha1"
	routev1 "github.com/openshift/api/route/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
	"sigs.k8s.io/controller-runtime/pkg/source"
)

var log = logf.Log.WithName("controller_zenithrapp")

// Add creates a new DecisionService Controller and adds it to the Manager. The Manager will set fields on the Controller
// and Start it when the Manager is Started.
func Add(mgr manager.Manager) error {
	return add(mgr, newReconciler(mgr))
}

// newReconciler returns a new reconcile.Reconciler
func newReconciler(mgr manager.Manager) reconcile.Reconciler {
	return &ReconcileDecisionService{client: mgr.GetClient(), scheme: mgr.GetScheme()}
}

// add adds a new Controller to mgr with r as the reconcile.Reconciler
func add(mgr manager.Manager, r reconcile.Reconciler) error {
	// Create a new controller
	c, err := controller.New("zenithrapp-controller", mgr, controller.Options{Reconciler: r})
	if err != nil {
		return err
	}

	// Watch for changes to primary resource DecisionService
	err = c.Watch(&source.Kind{Type: &zenithrv1.DecisionService{}}, &handler.EnqueueRequestForObject{})
	if err != nil {
		return err
	}

	// Watch for changes to secondary resource Pods and Services, and requeue the owner DecisionService
	if err = watchResources(c, &corev1.Pod{}); err != nil {
		return err
	}
	if err = watchResources(c, &corev1.Service{}); err != nil {
		return err
	}

	// Watch for changes to knative Service, and requeue the owner DecisionService
	if err = watchResources(c, &knative.Service{}); err != nil {
		return err
	}
	return nil
}

func watchResources(controller controller.Controller, resource runtime.Object) error {
	return controller.Watch(&source.Kind{Type: resource}, &handler.EnqueueRequestForOwner{
		IsController: true,
		OwnerType:    &zenithrv1.DecisionService{},
	})
}

var _ reconcile.Reconciler = &ReconcileDecisionService{}

// ReconcileDecisionService reconciles a DecisionService object
type ReconcileDecisionService struct {
	// This client, initialized using mgr.Client() above, is a split client
	// that reads objects from the cache and writes to the apiserver
	client client.Client
	scheme *runtime.Scheme
}

type KubeObject interface {
	runtime.Object
	metav1.Object
}

// Reconcile reads that state of the cluster for a DecisionService object and makes changes based on the state read
// and what is in the DecisionService.Spec
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileDecisionService) Reconcile(request reconcile.Request) (reconcile.Result, error) {
	reqLogger := log.WithValues("Request.Namespace", request.Namespace, "Request.Name", request.Name)
	reqLogger.Info("Reconciling DecisionService")

	// Fetch the DecisionService instance
	instance := &zenithrv1.DecisionService{}
	err := r.client.Get(context.TODO(), request.NamespacedName, instance)
	if err != nil {
		if errors.IsNotFound(err) {
			// Request object not found, could have been deleted after reconcile request.
			// Owned objects are automatically garbage collected. For additional cleanup logic use finalizers.
			// Return and don't requeue
			return reconcile.Result{}, nil
		}
		// Error reading the object - requeue the request.
		return reconcile.Result{}, err
	}

	if len(instance.Spec.Name) == 0 {
		instance.Spec.Name = instance.Name
	}

	if instance.Spec.KNative {
		genKService := newKService(instance)
		curKService := &knative.Service{}
		err = r.loadOrCreate(instance, genKService, curKService)
		if err != nil {
			return reconcile.Result{}, err
		} else if existed(curKService) {
			curContainer := curKService.Spec.RunLatest.Configuration.RevisionTemplate.Spec.Container
			genContainer := genKService.Spec.RunLatest.Configuration.RevisionTemplate.Spec.Container
			updated, err := changed(curContainer, genContainer)
			if err != nil {
				reqLogger.Info("Detected that knative service remains unchanged")
				return reconcile.Result{}, err
			} else if updated {
				reqLogger.Info("Detected that knative service needs to be updated")
				genKService.SetResourceVersion(curKService.GetResourceVersion())
				err = r.client.Update(context.TODO(), genKService)
				if err != nil {
					return reconcile.Result{}, err
				} else {
					return reconcile.Result{Requeue: true}, nil
				}
			} else {
				if instance.Status.RouteHost != getHostname(curKService.Status.Domain) {
					log.Info("Will set hostname to", "hostname", curKService.Status.Domain)
					instance.Status.RouteHost = getHostname(curKService.Status.Domain)
					err = r.client.Update(context.TODO(), instance)
					if err != nil {
						log.Error(err, "Error updating CR", "cr", instance)
						return reconcile.Result{}, err
					}
				}

			}
		}
		return reconcile.Result{Requeue: true}, nil
	}

	// Create pod object based on CR, if does not exist:
	genPod := newPodForCR(instance)
	curPod := &corev1.Pod{}
	err = r.loadOrCreate(instance, genPod, curPod)
	if err != nil {
		return reconcile.Result{}, err
	} else if existed(curPod) {
		curContainer := curPod.Spec.Containers[0]
		genContainer := genPod.Spec.Containers[0]
		updated, err := changed(curContainer, genContainer)
		if err != nil {
			return reconcile.Result{}, err
		} else if updated {
			reqLogger.Info("Detected that pod needs to be updated, will delete it and let it be recreated!")
			err = r.client.Delete(context.TODO(), curPod)
			if err != nil {
				return reconcile.Result{}, err
			} else {
				return reconcile.Result{Requeue: true}, nil
			}
		}
	}

	// Create service object based on CR, if does not exist:
	curService := &corev1.Service{}
	err = r.loadOrCreate(instance, newServiceForCR(instance), curService)
	if err != nil {
		return reconcile.Result{}, err
	}

	genRoute := newRouteForCR(instance)
	curRoute := &routev1.Route{}
	if instance.Spec.Expose {
		// Create route based on CR, if does not exist:
		err = r.loadOrCreate(instance, genRoute, curRoute)
		if err != nil {
			return reconcile.Result{}, err
		} else if existed(curRoute) {
			if len(instance.Spec.HostName) > 0 && instance.Spec.HostName != curRoute.Spec.Host {
				reqLogger.Info("Detected that route hostname needs to be updated!")
				curRoute.Spec.Host = instance.Spec.HostName
				err = r.client.Update(context.TODO(), curRoute)
				if err != nil {
					return reconcile.Result{}, err
				} else {
					//Status URL should next be updated based on this
					return reconcile.Result{Requeue: true}, nil
				}
			}
		}
	} else {
		err := r.client.Get(context.TODO(), types.NamespacedName{Name: genRoute.Name, Namespace: genRoute.Namespace}, curRoute)
		if err == nil {
			//There is a route from before, but delete it, since expose flag has been removed
			reqLogger.Info("Will delete old route")
			err = r.client.Delete(context.TODO(), curRoute)
			if err != nil {
				reqLogger.Info("Error deleting", "error", err)
				return reconcile.Result{}, err
			}
		} else if errors.IsNotFound(err) {
			//There is no existing route, nor should there be one, so all is good
		} else {
			//Unknown error
			reqLogger.Info("Error finding out if there was an old route", "error", err)
			return reconcile.Result{}, err
		}
	}
	if instance.Spec.Expose {
		if len(curRoute.Name) == 0 {
			//Route must have been just created, let's set URL status later
			retryTime := 5
			reqLogger.Info("Will try reconciliation again to set status hostname", "retry time", retryTime)
			return reconcile.Result{Requeue: true, RequeueAfter: time.Duration(retryTime) * time.Second}, nil
		}
		if instance.Status.RouteHost != getHostname(curRoute.Spec.Host) {
			err := r.setRouteHostname(instance, *curRoute)
			if err != nil {
				reqLogger.Error(err, "Error setting route hostname")
				return reconcile.Result{}, err
			} else {
				retryTime := 5
				reqLogger.Info("Should have updated route host, but will try reconciliation again to verify", "retry time", retryTime)
				return reconcile.Result{Requeue: true, RequeueAfter: time.Duration(retryTime) * time.Second}, nil
			}
		}
	} else if len(instance.Status.RouteHost) > 0 {
		instance.Status.RouteHost = ""
		err = r.client.Update(context.TODO(), instance)
		if err != nil {
			log.Error(err, "Error updating CR", "cr", instance)
			return reconcile.Result{}, err
		}
	}
	return reconcile.Result{}, nil
}

// newPodForCR returns a functioning pod with the same name/namespace as the cr
func newPodForCR(cr *zenithrv1.DecisionService) *corev1.Pod {
	labels := map[string]string{
		"app": cr.Name,
	}
	return &corev1.Pod{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name,
			Namespace: cr.Namespace,
			Labels:    labels,
		},
		Spec: corev1.PodSpec{
			Containers: []corev1.Container{
				{
					Name:  cr.Name,
					Image: "quay.io/bmozaffa/zenithr",
					Env: []corev1.EnvVar{
						{
							Name:  GETRules,
							Value: getJson(cr.Spec),
						},
					},
					ReadinessProbe: &corev1.Probe{
						Handler: corev1.Handler{
							HTTPGet: &corev1.HTTPGetAction{
								Path: "health",
								Port: intstr.IntOrString{IntVal: 8080},
							},
						},
						InitialDelaySeconds: 5,
						PeriodSeconds:       3,
						FailureThreshold:    20,
					},
					LivenessProbe: &corev1.Probe{
						Handler: corev1.Handler{
							HTTPGet: &corev1.HTTPGetAction{
								Path: "health",
								Port: intstr.IntOrString{IntVal: 8080},
							},
						},
						InitialDelaySeconds: 60,
						PeriodSeconds:       60,
					},
				},
			},
		},
	}
}

// newServiceForCR returns a service that directs to the application pod
func newServiceForCR(cr *zenithrv1.DecisionService) *corev1.Service {
	labels := map[string]string{
		"app": cr.Name,
	}
	return &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name,
			Namespace: cr.Namespace,
			Labels:    labels,
		},
		Spec: corev1.ServiceSpec{
			Ports: []corev1.ServicePort{
				{
					Name: "http",
					Port: 8080,
				},
			},
			Selector: labels,
		},
	}
}

// newRouteForCR returns a route that exposes the application service
func newRouteForCR(cr *zenithrv1.DecisionService) *routev1.Route {
	labels := map[string]string{
		"app": cr.Name,
	}
	route := routev1.Route{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name,
			Namespace: cr.Namespace,
			Labels:    labels,
		},
		Spec: routev1.RouteSpec{
			To: routev1.RouteTargetReference{
				Name: cr.Name,
			},
		},
	}
	if len(cr.Spec.HostName) > 0 {
		route.Spec.Host = cr.Spec.HostName
	}
	route.SetGroupVersionKind(routev1.SchemeGroupVersion.WithKind("Route"))
	return &route
}

func newKService(cr *zenithrv1.DecisionService) *knative.Service {
	labels := map[string]string{
		"app": cr.Name,
	}
	klabels := map[string]string{
		"knative.dev/type": "container",
	}
	service := knative.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name,
			Namespace: cr.Namespace,
			Labels:    labels,
		},
		Spec: knative.ServiceSpec{
			RunLatest: &knative.RunLatestType{
				Configuration: knative.ConfigurationSpec{
					RevisionTemplate: knative.RevisionTemplateSpec{
						ObjectMeta: metav1.ObjectMeta{
							Labels: klabels,
						},
						Spec: knative.RevisionSpec{
							Container: corev1.Container{
								Image:           "quay.io/bmozaffa/zenithr",
								ImagePullPolicy: corev1.PullAlways,
								Env: []corev1.EnvVar{
									{
										Name:  GETRules,
										Value: getJson(cr.Spec),
									},
								},
							},
						},
					},
				},
			},
		},
	}
	service.SetGroupVersionKind(knative.SchemeGroupVersion.WithKind("Service"))
	return &service
}

func getJson(spec zenithrv1.DecisionServiceSpec) string {
	bytes, err := json.Marshal(spec)
	if err != nil {
		panic("Failed to parse input!")
	}
	return string(bytes)
}

func (r *ReconcileDecisionService) setRouteHostname(cr *zenithrv1.DecisionService, route routev1.Route) (err error) {
	hostname := getHostname(route.Spec.Host)
	if len(hostname) > 0 {
		err = r.client.Get(context.TODO(), types.NamespacedName{Name: cr.Name, Namespace: cr.Namespace}, cr)
		if err != nil {
			log.Error(err, "Error Reloading CR", "cr", cr)
			return
		}
		log.Info("Will set route hostname to", "hostname", hostname)
		cr.Status.RouteHost = hostname
		err = r.client.Update(context.TODO(), cr)
		if err != nil {
			log.Error(err, "Error updating CR", "cr", cr)
			return
		}
	}
	return
}

func getHostname(routeHost string) string {
	if len(routeHost) > 0 {
		return fmt.Sprintf("http://%s", routeHost)
	} else {
		return ""
	}
}

func (r *ReconcileDecisionService) loadOrCreate(instance *zenithrv1.DecisionService, genObject KubeObject, curObject KubeObject) error {
	reqLogger := log.WithValues("Request.Namespace", instance.Namespace, "Request.Name", instance.Name)
	// Set DecisionService instance as the owner and controller
	if err := controllerutil.SetControllerReference(instance, genObject, r.scheme); err != nil {
		return err
	}
	//Check if this object already exists
	err := r.client.Get(context.TODO(), types.NamespacedName{Name: genObject.GetName(), Namespace: genObject.GetNamespace()}, curObject)
	if err != nil && errors.IsNotFound(err) {
		reqLogger.Info("Creating a new Object", "type", reflect.TypeOf(genObject), "Namespace", genObject.GetNamespace(), "Name", genObject.GetName())
		err = r.client.Create(context.TODO(), genObject)
		if err != nil {
			reqLogger.Info("Got an error creating it", "error", err)
			return err
		}
		// Object created successfully - don't requeue
		return nil
	} else if err != nil {
		reqLogger.Info("Got an error looking it up", "error", err)
		return err
	} else {
		return nil
	}
}

func changed(current corev1.Container, generated corev1.Container) (changed bool, err error) {
	currentRules := getEnvVar(current.Env, GETRules)
	var currentSpec zenithrv1.DecisionServiceSpec
	err = json.Unmarshal([]byte(currentRules), &currentSpec)
	if err != nil {
		return
	}
	generatedRules := getEnvVar(generated.Env, GETRules)
	var generatedSpec zenithrv1.DecisionServiceSpec
	err = json.Unmarshal([]byte(generatedRules), &generatedSpec)
	if err != nil {
		return
	}
	if !reflect.DeepEqual(currentSpec, generatedSpec) {
		changed = true
	}
	return
}

func getEnvVar(vars []corev1.EnvVar, key string) string {
	for _, envVar := range vars {
		if envVar.Name == key {
			return envVar.Value
		}
	}
	return ""
}

func existed(object KubeObject) bool {
	return len(object.GetName()) > 0
}
