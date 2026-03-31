#!/bin/sh
# Auto-select Ollama model based on hardware accelerator availability.
# Default: llama3.2:3b (CPU-safe, ~2 GB RAM).
# Upgrades to llama3.1:8b when NVIDIA GPU (nvidia-smi) or AMD GPU (rocm-smi) is detected.
# Apple Silicon / Neural Engine: Ollama uses Metal automatically on macOS — override via OLLAMA_MODEL env var.
# Override at any time by setting OLLAMA_MODEL before starting the container.
if [ -z "$OLLAMA_MODEL" ]; then
  if nvidia-smi > /dev/null 2>&1 || rocm-smi > /dev/null 2>&1; then
    export OLLAMA_MODEL="llama3.1:8b"
    echo "GPU/neural engine detected — using $OLLAMA_MODEL"
  else
    export OLLAMA_MODEL="llama3.2:3b"
    echo "No accelerator detected — using $OLLAMA_MODEL"
  fi
fi
exec java -jar /app/build.jar
