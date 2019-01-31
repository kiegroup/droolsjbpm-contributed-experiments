package v1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// DecisionServiceSpec defines the desired state of DecisionService
type DecisionServiceSpec struct {
	Name     string     `json:"name,omitempty"`
	Input    []Variable `json:"input"`
	Rules    []Rules    `json:"rules"`
	Output   OutputType `json:"output"`
	Expose   bool       `json:"expose,omitempty"`
	HostName string     `json:"hostname,omitempty"`

	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file
}

type Variable struct {
	Name string `json:"name"`
	Type string `json:"type"`
}

type Rules struct {
	When string `json:"when"`
	Then Action `json:"then"`
}

type Action struct {
	Output string `json:"output"`
}

type OutputType struct {
	Type string `json:"type"`
}

// DecisionServiceStatus defines the observed state of DecisionService
type DecisionServiceStatus struct {
	RouteHost string      `json:"routeHost,omitempty"`
	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// DecisionService is the Schema for the zenithrapps API
// +k8s:openapi-gen=true
type DecisionService struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   DecisionServiceSpec   `json:"spec,omitempty"`
	Status DecisionServiceStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// DecisionServiceList contains a list of DecisionService
type DecisionServiceList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []DecisionService `json:"items"`
}

func init() {
	SchemeBuilder.Register(&DecisionService{}, &DecisionServiceList{})
}
