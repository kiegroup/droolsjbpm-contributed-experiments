package apis

import (
	"github.com/kiegroup/zenithr-operator/pkg/apis/zenithr/v1"
	knative "github.com/knative/serving/pkg/apis/serving/v1alpha1"
	routev1 "github.com/openshift/api/route/v1"
)

func init() {
	// Register the types with the Scheme so the components can map objects to GroupVersionKinds and back
	AddToSchemes = append(AddToSchemes,
		v1.SchemeBuilder.AddToScheme,
		routev1.SchemeBuilder.AddToScheme,
		knative.SchemeBuilder.AddToScheme,
	)
}
