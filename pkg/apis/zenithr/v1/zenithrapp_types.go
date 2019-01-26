package v1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// ZenithrAppSpec defines the desired state of ZenithrApp
type ZenithrAppSpec struct {
	Input    []Variable      `json:"input"`
	Rules    []Rules         `json:"rules"`
	Output   OutputType      `json:"output"`

	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file
}

type Variable struct {
	Name    string      `json:"name"`
	Type    string      `json:"type"`
}

type Rules struct {
	When    string      `json:"when"`
	Then    Action      `json:"then"`
}

type Action struct {
	Output  string      `json:"output"`
}

type OutputType struct {
	Type    string      `json:"type"`
}

// ZenithrAppStatus defines the observed state of ZenithrApp
type ZenithrAppStatus struct {
	// INSERT ADDITIONAL STATUS FIELD - define observed state of cluster
	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// ZenithrApp is the Schema for the zenithrapps API
// +k8s:openapi-gen=true
type ZenithrApp struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   ZenithrAppSpec   `json:"spec,omitempty"`
	Status ZenithrAppStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// ZenithrAppList contains a list of ZenithrApp
type ZenithrAppList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []ZenithrApp `json:"items"`
}

func init() {
	SchemeBuilder.Register(&ZenithrApp{}, &ZenithrAppList{})
}
