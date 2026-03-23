#!/bin/sh
# Auto-select Ollama model based on hardware accelerator availability.
# Default: qwen2.5:7b (CPU-safe, ~6 GB RAM).
# Upgrades to llama3.1:8b when NVIDIA GPU (nvidia-smi) or AMD GPU (rocm-smi) is detected.
# Apple Silicon / Neural Engine: Ollama uses Metal automatically on macOS — override via OLLAMA_MODEL env var.
# Override at any time by setting OLLAMA_MODEL before starting the container.
if [ -z "$OLLAMA_MODEL" ]; then
  if nvidia-smi > /dev/null 2>&1 || rocm-smi > /dev/null 2>&1; then
    export OLLAMA_MODEL="llama3.1:8b"
    echo "GPU/neural engine detected — using $OLLAMA_MODEL"
  else
    export OLLAMA_MODEL="qwen2.5:7b"
    echo "No accelerator detected — using $OLLAMA_MODEL"
  fi
fi
exec java -jar /app/build.jar
