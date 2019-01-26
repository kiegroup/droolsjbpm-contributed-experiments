package zenithrapp

import (
	"encoding/json"
	"github.com/kiegroup/zenithr-operator/pkg/apis/zenithr/v1"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestGetJson(t *testing.T) {
	cr := &v1.ZenithrApp{
		Spec: v1.ZenithrAppSpec{
			Input: []v1.Variable{
				{
					Name: "grade",
					Type: "double",
				},
			},
			Rules: []v1.Rules{
				{
					When: "grade >= 90 && grade <= 100",
					Then: v1.Action{
						Output: "A",
					},
				},
				{
					When: "grade >= 80 && grade < 90",
					Then: v1.Action{
						Output: "B",
					},
				},
			},
			Output: v1.OutputType{
				Type: "string",
			},
		},
	}
	actualJsonString := getJson(cr.Spec)

	expectedJsonString := `
{
   "input":[
      {
         "name":"grade",
         "type":"double"
      }
   ],
   "rules":[
      {
         "when":"grade >= 90 && grade <= 100",
         "then":{
            "output":"A"
         }
      },
      {
         "when":"grade >= 80 && grade < 90",
         "then":{
            "output":"B"
         }
      }
   ],
   "output":{
      "type":"string"
   }
}
`

	assert.Equal(t, parseSpec(t, expectedJsonString), parseSpec(t, actualJsonString), "Expected json value to be the same as provided")
}

func parseSpec(t *testing.T, jsonString string) *v1.ZenithrAppSpec {
	spec := &v1.ZenithrAppSpec{}
	err := json.Unmarshal([]byte(jsonString), spec)
	assert.NoError(t, err, "Should be able to unmarshal this json with no error %v", err)
	return spec
}
