package zenithrapp

import (
	"context"
	"encoding/json"
	"fmt"
	"k8s.io/apimachinery/pkg/util/intstr"
	"reflect"
	"time"

	zenithrv1 "github.com/kiegroup/zenithr-operator/pkg/apis/zenithr/v1"
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

// Add creates a new ZenithrApp Controller and adds it to the Manager. The Manager will set fields on the Controller
// and Start it when the Manager is Started.
func Add(mgr manager.Manager) error {
	return add(mgr, newReconciler(mgr))
}

// newReconciler returns a new reconcile.Reconciler
func newReconciler(mgr manager.Manager) reconcile.Reconciler {
	return &ReconcileZenithrApp{client: mgr.GetClient(), scheme: mgr.GetScheme()}
}

// add adds a new Controller to mgr with r as the reconcile.Reconciler
func add(mgr manager.Manager, r reconcile.Reconciler) error {
	// Create a new controller
	c, err := controller.New("zenithrapp-controller", mgr, controller.Options{Reconciler: r})
	if err != nil {
		return err
	}

	// Watch for changes to primary resource ZenithrApp
	err = c.Watch(&source.Kind{Type: &zenithrv1.ZenithrApp{}}, &handler.EnqueueRequestForObject{})
	if err != nil {
		return err
	}

	// Watch for changes to secondary resource Pods and Services, and requeue the owner ZenithrApp
	if err = watchResources(c, &corev1.Pod{}); err != nil {
		return err
	}
	if err = watchResources(c, &corev1.Service{}); err != nil {
		return err
	}
	return nil
}

func watchResources(controller controller.Controller, resource runtime.Object) error {
	return controller.Watch(&source.Kind{Type: resource}, &handler.EnqueueRequestForOwner{
		IsController: true,
		OwnerType:    &zenithrv1.ZenithrApp{},
	})
}

var _ reconcile.Reconciler = &ReconcileZenithrApp{}

// ReconcileZenithrApp reconciles a ZenithrApp object
type ReconcileZenithrApp struct {
	// This client, initialized using mgr.Client() above, is a split client
	// that reads objects from the cache and writes to the apiserver
	client client.Client
	scheme *runtime.Scheme
}

type KubeObject interface {
	runtime.Object
	metav1.Object
}

// Reconcile reads that state of the cluster for a ZenithrApp object and makes changes based on the state read
// and what is in the ZenithrApp.Spec
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileZenithrApp) Reconcile(request reconcile.Request) (reconcile.Result, error) {
	reqLogger := log.WithValues("Request.Namespace", request.Namespace, "Request.Name", request.Name)
	reqLogger.Info("Reconciling ZenithrApp")

	// Fetch the ZenithrApp instance
	instance := &zenithrv1.ZenithrApp{}
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

	// Create pod object based on CR, if does not exist:
	err = r.create(instance, newPodForCR(instance), &corev1.Pod{})
	if err != nil {
		return reconcile.Result{}, err
	}

	// Create service object based on CR, if does not exist:
	err = r.create(instance, newServiceForCR(instance), &corev1.Service{})
	if err != nil {
		return reconcile.Result{}, err
	}

	genRoute := newRouteForCR(instance)
	curRoute := &routev1.Route{}
	if instance.Spec.Expose {
		// Create route based on CR, if does not exist:
		err = r.create(instance, genRoute, curRoute)
		if err != nil {
			return reconcile.Result{}, err
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
	if instance.Spec.Expose && len(instance.Status.RouteHost) == 0 {
		if len(curRoute.Name) == 0 {
			//Route must have been just created, let's set URL status later
			retryTime := 5
			reqLogger.Info("Will try reconciliation again to set status hostname", "retry time", retryTime)
			return reconcile.Result{Requeue:true, RequeueAfter:time.Duration(retryTime) * time.Second}, nil
		}
		err := r.setRouteHostname(instance, *curRoute)
		if err != nil {
			reqLogger.Error(err, "Error setting route hostname")
			return reconcile.Result{}, err
		} else {
			retryTime := 5
			reqLogger.Info("Should have updated route host, but will try reconciliation again to verify", "retry time", retryTime)
			return reconcile.Result{Requeue:true, RequeueAfter:time.Duration(retryTime) * time.Second}, nil
		}
	}
	return reconcile.Result{}, nil
}

// newPodForCR returns a functioning pod with the same name/namespace as the cr
func newPodForCR(cr *zenithrv1.ZenithrApp) *corev1.Pod {
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
							Name:  "GET",
							Value: getJson(cr.Spec),
						},
						{
							Name:  "NAME",
							Value: cr.Spec.Name,
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
func newServiceForCR(cr *zenithrv1.ZenithrApp) *corev1.Service {
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
func newRouteForCR(cr *zenithrv1.ZenithrApp) *routev1.Route {
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

func getJson(spec zenithrv1.ZenithrAppSpec) string {
	bytes, err := json.Marshal(spec)
	if err != nil {
		panic("Failed to parse input!")
	}
	return string(bytes)
}

func (r *ReconcileZenithrApp) setRouteHostname(cr *zenithrv1.ZenithrApp, route routev1.Route) (err error) {
	err = r.client.Get(context.TODO(), types.NamespacedName{Name: cr.Name, Namespace: cr.Namespace}, cr)
	if err != nil {
		log.Error(err, "Error Reloading CR", "cr", cr)
		return
	}
	if len(route.Spec.Host) > 0 {
		log.Info("Will set route hostname to", "hostname", route.Spec.Host)
		cr.Status.RouteHost = fmt.Sprintf("http://%s", route.Spec.Host)
		err = r.client.Update(context.TODO(), cr)
		if err != nil {
			log.Error(err, "Error updating CR", "cr", cr)
			return
		}
	}
	return
}

func (r *ReconcileZenithrApp) create(instance *zenithrv1.ZenithrApp, genObject KubeObject, curObject KubeObject) error {
	reqLogger := log.WithValues("Request.Namespace", instance.Namespace, "Request.Name", instance.Name)
	// Set ZenithrApp instance as the owner and controller
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
