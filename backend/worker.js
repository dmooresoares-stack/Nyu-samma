const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
  "Cache-Control": "no-store"
};

function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json; charset=utf-8" }
  });
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === "OPTIONS") return new Response(null, { headers: corsHeaders });
    if (url.pathname !== "/chat") return json({ error: "Not found" }, 404);
    if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);
    if (!env.OPENAI_API_KEY || !env.NYU_APP_TOKEN) return json({ error: "Server is not configured" }, 503);
    if (request.headers.get("Authorization") !== "Bearer " + env.NYU_APP_TOKEN) return json({ error: "Unauthorized" }, 401);

    let body;
    try { body = await request.json(); }
    catch (_) { return json({ error: "Invalid JSON" }, 400); }

    const message = typeof body.message === "string" ? body.message.trim() : "";
    if (!message || message.length > 800) return json({ error: "Message must be between 1 and 800 characters" }, 400);

    const input = [];
    const history = Array.isArray(body.history) ? body.history.slice(-8) : [];
    for (const item of history) {
      if (!item || !["user", "assistant"].includes(item.role) || typeof item.content !== "string") continue;
      const content = item.content.trim().slice(0, 800);
      if (content) input.push({ role: item.role, content });
    }
    input.push({ role: "user", content: message });

    let upstream;
    try {
      upstream = await fetch("https://api.openai.com/v1/responses", {
        method: "POST",
        headers: {
          "Authorization": "Bearer " + env.OPENAI_API_KEY,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          model: env.OPENAI_MODEL || "gpt-5-mini",
          instructions: "Você é a Nyu Samma, uma assistente de voz afetuosa do aplicativo da cachorrinha Lhasa Apso do usuário. Converse em português brasileiro, naturalmente, com carinho e de forma concisa para ser lida em voz alta. O usuário gosta de ser chamado de Fofolete. Você é uma IA no aplicativo, não diga que é a cachorrinha real. Use somente texto simples.",
          input,
          max_output_tokens: 180,
          store: false
        })
      });
    } catch (_) { return json({ error: "AI service unavailable" }, 502); }

    if (!upstream.ok) return json({ error: "AI request failed" }, 502);
    let result;
    try { result = await upstream.json(); }
    catch (_) { return json({ error: "Invalid AI response" }, 502); }

    const reply = (result.output || [])
      .filter(item => item.type === "message")
      .flatMap(item => item.content || [])
      .filter(part => part.type === "output_text" && typeof part.text === "string")
      .map(part => part.text)
      .join(" ")
      .trim();
    if (!reply) return json({ error: "AI returned no text" }, 502);
    return json({ reply });
  }
};
