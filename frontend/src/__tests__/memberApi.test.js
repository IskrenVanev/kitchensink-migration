import { describe, it, expect, vi, beforeEach } from "vitest";
import { getMembers, createMember } from "../services/memberApi";

describe("getMembers", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("returns member array on successful response", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () =>
        Promise.resolve([
          { id: 1, name: "Alice", email: "alice@example.com", phoneNumber: "2125551212" },
        ]),
    });

    const result = await getMembers();

    expect(result).toHaveLength(1);
    expect(result[0].name).toBe("Alice");
  });

  it("returns empty array when response data is not an array", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(null),
    });

    const result = await getMembers();

    expect(result).toEqual([]);
  });

  it("returns empty array when response data is an object", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ unexpected: "object" }),
    });

    const result = await getMembers();

    expect(result).toEqual([]);
  });

  it("throws error when response is not ok", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: () => Promise.resolve({}),
    });

    await expect(getMembers()).rejects.toThrow("Unable to load members");
  });

  it("calls the correct endpoint", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve([]),
    });

    await getMembers();

    expect(global.fetch).toHaveBeenCalledWith("/rest/members");
  });
});

describe("createMember", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("returns ok=true and status=200 on successful creation", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve({}),
    });

    const result = await createMember({
      name: "Alice",
      email: "alice@example.com",
      phoneNumber: "2125551212",
    });

    expect(result.ok).toBe(true);
    expect(result.status).toBe(200);
  });

  it("posts to the correct endpoint with JSON content-type", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve({}),
    });

    await createMember({ name: "Alice", email: "alice@example.com", phoneNumber: "2125551212" });

    expect(global.fetch).toHaveBeenCalledWith(
      "/rest/members",
      expect.objectContaining({
        method: "POST",
        headers: expect.objectContaining({ "Content-Type": "application/json" }),
      })
    );
  });

  it("serialises the payload to JSON in the request body", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve({}),
    });

    const payload = { name: "Alice", email: "alice@example.com", phoneNumber: "2125551212" };
    await createMember(payload);

    const [, options] = global.fetch.mock.calls[0];
    expect(JSON.parse(options.body)).toEqual(payload);
  });

  it("returns ok=false with field error payload on 400 response", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      json: () => Promise.resolve({ name: "Must not contain numbers" }),
    });

    const result = await createMember({
      name: "Alice123",
      email: "alice@example.com",
      phoneNumber: "2125551212",
    });

    expect(result.ok).toBe(false);
    expect(result.status).toBe(400);
    expect(result.errorPayload).toHaveProperty("name", "Must not contain numbers");
  });

  it("returns ok=false with status=409 on conflict response", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 409,
      json: () => Promise.resolve({ email: "Email taken" }),
    });

    const result = await createMember({
      name: "Alice",
      email: "taken@example.com",
      phoneNumber: "2125551212",
    });

    expect(result.ok).toBe(false);
    expect(result.status).toBe(409);
    expect(result.errorPayload.email).toBe("Email taken");
  });

  it("returns empty errorPayload when response JSON cannot be parsed", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      json: () => Promise.reject(new Error("parse error")),
    });

    const result = await createMember({
      name: "Alice",
      email: "alice@example.com",
      phoneNumber: "2125551212",
    });

    expect(result.ok).toBe(false);
    expect(result.errorPayload).toEqual({});
  });
});
