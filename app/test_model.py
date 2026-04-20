from llama_cpp import Llama

llm = Llama(
    model_path="models/qwen/qwen2-vl-2b-q4_k_m.gguf",
    n_ctx=2048
)

output = llm("What are symptoms of malaria?", max_tokens=100)
print(output["choices"][0]["text"])