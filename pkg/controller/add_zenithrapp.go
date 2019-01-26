package controller

import (
	"github.com/kiegroup/zenithr-operator/pkg/controller/zenithrapp"
)

func init() {
	// AddToManagerFuncs is a list of functions to create controllers and add them to a manager.
	AddToManagerFuncs = append(AddToManagerFuncs, zenithrapp.Add)
}
