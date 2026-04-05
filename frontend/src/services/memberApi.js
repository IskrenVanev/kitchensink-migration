export async function getMembers() {
  const response = await fetch("/rest/members");

  if (!response.ok) {
    throw new Error("Unable to load members");
  }

  const data = await response.json();
  return Array.isArray(data) ? data : [];
}

export async function createMember(memberPayload) {
  const response = await fetch("/rest/members", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(memberPayload)
  });

  const errorPayload = await response.json().catch(() => ({}));

  return {
    ok: response.ok,
    status: response.status,
    errorPayload
  };
}
