# Zenithr Operator

## Deploy to OpenShift using OLM

As cluster-admin and an OCP 3.11+ cluster with OLM installed, issue the following command:

```bash
# If using the default OLM namespace "operator-lifecycle-manager"
./hack/catalog.sh

# If using a different namespace for OLM
./hack/catalog.sh <namespace>
```

This will create a new `CatalogSource` and `ConfigMap`, allowing the OLM Catalog to see this Operator's `ClusterServiceVersion`.

## Deploy to OpenShift Manually

Globally and only once for the whole cluster:

```bash
oc create -f https://raw.githubusercontent.com/bmozaffa/zenithr-operator/master/deploy/crds/zenithr.crd.yaml
```

In a project:

```bash
oc create -f https://raw.githubusercontent.com/bmozaffa/zenithr-operator/master/deploy/combined.yaml
```

## Trigger a DecisionService deployment

```bash
oc create -f https://raw.githubusercontent.com/bmozaffa/zenithr-operator/master/deploy/examples/support_contacts.yaml
```
