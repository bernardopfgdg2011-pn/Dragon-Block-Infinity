#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$ROOT_DIR/objfbxloader-decompiled"
EXPORT_DIR="$ROOT_DIR/objfbxloader-import"
OUT_JAVA_TARGET="$EXPORT_DIR/src/main/java"
OUT_RESOURCE_TARGET="$EXPORT_DIR/src/main/resources"
PROJECT_FILE="$EXPORT_DIR/project.txt"

if [ ! -d "$SRC_DIR" ]; then
  echo "Diretório de origem não encontrado: $SRC_DIR" >&2
  exit 1
fi

mkdir -p "$OUT_JAVA_TARGET" "$OUT_RESOURCE_TARGET"
: > "$PROJECT_FILE"

copy_count=0
text_count=0

while IFS= read -r file; do
  rel_path="${file#"$SRC_DIR"/}"

  case "$rel_path" in
    com/brckv2/objfbxloader/*)
      dest="$OUT_JAVA_TARGET/$rel_path"
      ;;
    assets/com.brckv2.objfbxloader/*|fabric.mod.json|com.brckv2.objfbxloader.mixins.json|LICENSE_com.brckv2.objfbxloader)
      dest="$OUT_RESOURCE_TARGET/$rel_path"
      ;;
    *)
      continue
      ;;
  esac

  mkdir -p "$(dirname "$dest")"

  case "$rel_path" in
    *.png|*.jpg|*.jpeg|*.gif|*.webp|*.bmp|*.ico|*.so|*.dll|*.dylib|*.class|*.jar|*.zip|*.7z)
      cp -f "$file" "$dest" 2>/dev/null || true
      {
        echo "===== BINARY/PNG ====="
        echo "Origem: $rel_path"
        echo "Destino: ${dest#"$ROOT_DIR/"}"
        echo
      } >> "$PROJECT_FILE"
      copy_count=$((copy_count + 1))
      continue
      ;;
  esac

  cp -f "$file" "$dest"
  text_count=$((text_count + 1))
  copy_count=$((copy_count + 1))

  {
    echo "===== FILE ====="
    echo "Origem: $rel_path"
    echo "Destino: ${dest#"$ROOT_DIR/"}"
    echo "--- BEGIN CONTENT ---"
    cat "$file"
    echo
    echo "--- END CONTENT ---"
    echo
  } >> "$PROJECT_FILE"
done < <(find "$SRC_DIR" -type f | sort)

echo "Arquivos copiados para: $EXPORT_DIR" >&2
echo "Arquivos de texto adicionados a project.txt: $text_count" >&2
echo "Relatório salvo em: $PROJECT_FILE" >&2
