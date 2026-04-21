import os
import sqlite3
from datetime import datetime

# 🗄️ Database Setup
def create_usertable():
    conn = sqlite3.connect('users.db')
    c = conn.cursor()
        
    c.execute('CREATE TABLE IF NOT EXISTS chat_history(username TEXT, date TEXT, role TEXT, content TEXT)')
    conn.commit()
    conn.close()

def save_chat(role, content):
    conn = sqlite3.connect('users.db')
    c = conn.cursor()
    date_str = datetime.now().strftime("%Y-%m-%d")
    c.execute('INSERT INTO chat_history(username, date, role, content) VALUES (?,?,?,?)', ("default_user", date_str, role, content))
    conn.commit()
    conn.close()

# 🧠 Imports for Groq API & LLM
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
from langchain_classic.chains import RetrievalQA
from langchain_core.prompts import PromptTemplate
from langchain_groq import ChatGroq

DB_FAISS_PATH = "vectorstore/db_faiss"

def get_vectorstore():
    embedding_model = HuggingFaceEmbeddings(model="sentence-transformers/all-MiniLM-L6-v2")
    db = FAISS.load_local(DB_FAISS_PATH, embedding_model, allow_dangerous_deserialization=True)
    return db

def set_custom_prompt(custom_prompt_template):
    prompt = PromptTemplate(template=custom_prompt_template, input_variables=["context", "question"])
    return prompt

def load_llm():
    api_key = os.environ.get("GROQ_API_KEY", "")
    if not api_key:
        print("Error: GROQ_API_KEY environment variable is missing!")
        exit(1)
        
    llm = ChatGroq(
        model_name="llama-3.1-8b-instant", 
        api_key=api_key,
        temperature=0.5
    )
    return llm

def rag_chatbot():
    print("\n" + "="*50)
    print("🧠 Ask Medical Chatbot (Local RAG)")
    print("="*50)
    
    vectorstore = get_vectorstore()
    llm = load_llm()

    CUSTOM_PROMPT_TEMPLATE = """
You are a helpful and informative medical assistant chatbot.

Use the following context to answer the user's question to the best of your ability. If the context contains relevant information, summarize it even if it's not a complete medical answer. 
You can also use your general knowledge to supplement the answer if it helps explain the context, but prioritize the provided context.

Context: {context}
Question: {question}

Answer:
"""
    qa_chain = RetrievalQA.from_chain_type(
        llm=llm,
        chain_type="stuff",
        retriever=vectorstore.as_retriever(search_kwargs={'k': 3}),
        return_source_documents=True,
        chain_type_kwargs={'prompt': set_custom_prompt(CUSTOM_PROMPT_TEMPLATE)}
    )

    print("Type 'exit' or 'quit' to close the assistant.")
    while True:
        prompt = input(f"\nAsk your medical question: ")
        if prompt.lower() in ['exit', 'quit']:
            break
            
        if not prompt.strip():
            continue

        save_chat("user", prompt)
        clean_prompt = prompt.replace("'", "").replace('"', '').strip()

        print("Thinking...")
        try:
            response = qa_chain.invoke({'query': clean_prompt})
            result_to_show = response["result"]

            print("\n[Assistant]:")
            print(result_to_show)
            save_chat("assistant", result_to_show)
        except Exception as e:
            print(f"Error: {str(e)}")

def main():
    create_usertable()
    print("Welcome to Medical RAG Bot")

    rag_chatbot()
    print("Goodbye!")

if __name__ == "__main__":
    main()
