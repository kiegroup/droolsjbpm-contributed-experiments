package zenithrapp

import (
	"encoding/json"
	"github.com/kiegroup/zenithr-operator/pkg/apis/zenithr/v1"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestGetJson(t *testing.T) {
	actualJsonString := getJson(getSampleCR().Spec)
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

func getSampleCR() v1.DecisionService {
	return v1.DecisionService{
		Spec: v1.DecisionServiceSpec{
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

}

func parseSpec(t *testing.T, jsonString string) *v1.DecisionServiceSpec {
	spec := &v1.DecisionServiceSpec{}
	err := json.Unmarshal([]byte(jsonString), spec)
	assert.NoError(t, err, "Should be able to unmarshal this json with no error %v", err)
	return spec
}

func TestDetectRuleChanges(t *testing.T) {
	cr1 := getSampleCR()
	cr2 := getSampleCR()
	pod1 := newPodForCR(&cr1)
	pod2 := newPodForCR(&cr2)
	updated, err := changed(pod1, pod2)
	assert.NoError(t, err)
	assert.False(t, updated)

	cr2.Spec.Rules[0].Then.Output = "C"
	pod2 = newPodForCR(&cr2)
	updated, err = changed(pod1, pod2)
	assert.NoError(t, err)
	assert.True(t, updated)
}
