#!/bin/sh

if [[ -z ${1} ]]; then
    CATALOG_NS="operator-lifecycle-manager"
else
    CATALOG_NS=${1}
fi

CSV=`cat deploy/catalog_resources/zenithr-operator.v0.0.1.clusterserviceversion.yaml | sed -e 's/^/      /' | sed '0,/ /{s/      /    - /}'`
CRD=`cat deploy/crds/zenithr.crd.yaml | sed -e 's/^/      /' | sed '0,/ /{s/      /    - /}'`
PKG=`cat deploy/catalog_resources/zenithr.package.yaml | sed -e 's/^/      /' | sed '0,/ /{s/      /    - /}'`

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: zenithr-operator
  namespace: ${CATALOG_NS}
data:
  clusterServiceVersions: |
${CSV}
  customResourceDefinitions: |
${CRD}
  packages: >
${PKG}
EOF

cat <<EOF | kubectl apply -f -
apiVersion: operators.coreos.com/v1alpha1
kind: CatalogSource
metadata:
  name: zenithr-operator
  namespace: ${CATALOG_NS}
spec:
  configMap: zenithr-operator
  displayName: Zenithr Operator
  publisher: Red Hat
  sourceType: internal
status:
  configMapReference:
    name: zenithr-operator
    namespace: ${CATALOG_NS}
EOF
