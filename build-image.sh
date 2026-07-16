#!/usr/bin/env bash
set -euo pipefail

# example usage:
# ./build-image.sh worldwidewalter:1.0.1 --push

GHCR_IMAGE="ghcr.io/patrickwinti/worldwidewalter"

PUSH=0
POSITIONAL=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --push) PUSH=1; shift ;;
    -h|--help)./b
      echo "Usage: $0 [--push] [image:tag]"
      echo "  Default tag: worldwidewalter:local"
      echo "  --push: also tag and push to ${GHCR_IMAGE}:<version>"
      exit 0
      ;;
    *) POSITIONAL="$1"; shift ;;
  esac
done

IMAGE_TAG="${POSITIONAL:-worldwidewalter:local}"
VERSION="${IMAGE_TAG##*:}"

cd "$(dirname "$0")"

mvn clean package
docker build -t "$IMAGE_TAG" .

LATEST_TAG="${IMAGE_TAG%:*}:latest"
docker tag "$IMAGE_TAG" "$LATEST_TAG"

echo
echo "Built $IMAGE_TAG and $LATEST_TAG"

if [[ "$PUSH" == "1" ]]; then
  REMOTE_TAG="${GHCR_IMAGE}:${VERSION}"
  REMOTE_LATEST="${GHCR_IMAGE}:latest"
  docker tag "$IMAGE_TAG" "$REMOTE_TAG"
  docker tag "$IMAGE_TAG" "$REMOTE_LATEST"
  docker push "$REMOTE_TAG"
  docker push "$REMOTE_LATEST"
  echo "Pushed $REMOTE_TAG and $REMOTE_LATEST"
else
  echo "Run with: docker run --rm -p 8080:8080 $IMAGE_TAG"
fi
