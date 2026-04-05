import React from "react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import App from "../App";
import * as memberApi from "../services/memberApi";

vi.mock("../services/memberApi");

const defaultMembers = [
  { id: 1, name: "John Smith", email: "john.smith@mailinator.com", phoneNumber: "2125551212" },
];

describe("App", () => {
  beforeEach(() => {
    memberApi.getMembers.mockResolvedValue(defaultMembers);
    memberApi.createMember.mockResolvedValue({ ok: true, status: 200, errorPayload: {} });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  // --- Initial render ---

  it("renders the main heading", () => {
    render(<App />);
    expect(screen.getByText("Welcome to Kitchensink Modernized")).toBeInTheDocument();
  });

  it("renders the Members section heading", () => {
    render(<App />);
    expect(screen.getByText("Members")).toBeInTheDocument();
  });

  it("calls getMembers on mount", async () => {
    render(<App />);
    await waitFor(() => expect(memberApi.getMembers).toHaveBeenCalledOnce());
  });

  it("displays loaded members in the table after mount", async () => {
    render(<App />);
    await waitFor(() => expect(screen.getByText("John Smith")).toBeInTheDocument());
    expect(screen.getByText("john.smith@mailinator.com")).toBeInTheDocument();
  });

  it("shows 'No registered members.' when list is empty", async () => {
    memberApi.getMembers.mockResolvedValue([]);
    render(<App />);
    await waitFor(() =>
      expect(screen.getByText("No registered members.")).toBeInTheDocument()
    );
  });

  it("shows global error when getMembers fails on mount", async () => {
    memberApi.getMembers.mockRejectedValue(new Error("Unable to load members"));
    render(<App />);
    await waitFor(() =>
      expect(screen.getByText("Unable to load members")).toBeInTheDocument()
    );
  });

  // --- Form interaction ---

  it("renders the MemberForm registration form", () => {
    render(<App />);
    expect(screen.getByText("Member Registration")).toBeInTheDocument();
  });

  it("renders the Sidebar", () => {
    render(<App />);
    expect(screen.getByAltText("Spring Boot")).toBeInTheDocument();
  });

  // --- Successful registration ---

  it("shows success message after successful registration", async () => {
    const user = userEvent.setup();
    memberApi.getMembers.mockResolvedValue([]);
    render(<App />);
    await waitFor(() => expect(memberApi.getMembers).toHaveBeenCalled());

    await user.type(screen.getByLabelText(/name:/i), "Alice");
    await user.type(screen.getByLabelText(/email:/i), "alice@example.com");
    await user.type(screen.getByLabelText(/phone #:/i), "2125551212");
    await user.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() =>
      expect(screen.getByText("Member registered successfully.")).toBeInTheDocument()
    );
  });

  it("clears the form after successful registration", async () => {
    const user = userEvent.setup();
    memberApi.getMembers.mockResolvedValue([]);
    render(<App />);
    await waitFor(() => expect(memberApi.getMembers).toHaveBeenCalled());

    await user.type(screen.getByLabelText(/name:/i), "Alice");
    await user.type(screen.getByLabelText(/email:/i), "alice@example.com");
    await user.type(screen.getByLabelText(/phone #:/i), "2125551212");
    await user.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() =>
      expect(screen.getByText("Member registered successfully.")).toBeInTheDocument()
    );
    expect(screen.getByLabelText(/name:/i)).toHaveValue("");
    expect(screen.getByLabelText(/email:/i)).toHaveValue("");
    expect(screen.getByLabelText(/phone #:/i)).toHaveValue("");
  });

  it("reloads the member list after successful registration", async () => {
    const user = userEvent.setup();
    memberApi.getMembers.mockResolvedValue([]);
    render(<App />);
    await waitFor(() => expect(memberApi.getMembers).toHaveBeenCalledOnce());

    await user.type(screen.getByLabelText(/name:/i), "Alice");
    await user.type(screen.getByLabelText(/email:/i), "alice@example.com");
    await user.type(screen.getByLabelText(/phone #:/i), "2125551212");
    await user.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() =>
      expect(memberApi.getMembers).toHaveBeenCalledTimes(2)
    );
  });

  // --- Validation error (400) ---

  it("shows field errors when API returns 400", async () => {
    const user = userEvent.setup();
    memberApi.getMembers.mockResolvedValue([]);
    memberApi.createMember.mockResolvedValue({
      ok: false,
      status: 400,
      errorPayload: { name: "Must not contain numbers" },
    });
    render(<App />);
    await waitFor(() => expect(memberApi.getMembers).toHaveBeenCalled());

    await user.type(screen.getByLabelText(/name:/i), "Alice123");
    await user.type(screen.getByLabelText(/email:/i), "alice@example.com");
    await user.type(screen.getByLabelText(/phone #:/i), "2125551212");
    await user.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() =>
      expect(screen.getByText("Must not contain numbers")).toBeInTheDocument()
    );
  });

  it("does not show success message when API returns 400", async () => {
    const user = userEvent.setup();
    memberApi.getMembers.mockResolvedValue([]);
    memberApi.createMember.mockResolvedValue({
      ok: false,
      status: 400,
      errorPayload: { name: "Must not contain numbers" },
    });
    render(<App />);
    await waitFor(() => expect(memberApi.getMembers).toHaveBeenCalled());

    await user.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() => expect(memberApi.createMember).toHaveBeenCalled());
    expect(screen.queryByText("Member registered successfully.")).not.toBeInTheDocument();
  });

  // --- Duplicate email (409) ---

  it("shows 'Email taken' field error when API returns 409", async () => {
    const user = userEvent.setup();
    memberApi.getMembers.mockResolvedValue([]);
    memberApi.createMember.mockResolvedValue({
      ok: false,
      status: 409,
      errorPayload: { email: "Email taken" },
    });
    render(<App />);
    await waitFor(() => expect(memberApi.getMembers).toHaveBeenCalled());

    await user.type(screen.getByLabelText(/name:/i), "Alice");
    await user.type(screen.getByLabelText(/email:/i), "taken@example.com");
    await user.type(screen.getByLabelText(/phone #:/i), "2125551212");
    await user.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() =>
      expect(screen.getByText("Email taken")).toBeInTheDocument()
    );
  });

  // --- Network / unexpected error ---

  it("shows global error when createMember throws a network error", async () => {
    const user = userEvent.setup();
    memberApi.getMembers.mockResolvedValue([]);
    memberApi.createMember.mockRejectedValue(new Error("Network error"));
    render(<App />);
    await waitFor(() => expect(memberApi.getMembers).toHaveBeenCalled());

    await user.type(screen.getByLabelText(/name:/i), "Alice");
    await user.type(screen.getByLabelText(/email:/i), "alice@example.com");
    await user.type(screen.getByLabelText(/phone #:/i), "2125551212");
    await user.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() =>
      expect(screen.getByText("Network error")).toBeInTheDocument()
    );
  });

  it("shows fallback global error message when createMember returns non-400/409", async () => {
    const user = userEvent.setup();
    memberApi.getMembers.mockResolvedValue([]);
    memberApi.createMember.mockResolvedValue({
      ok: false,
      status: 500,
      errorPayload: { error: "Internal server error" },
    });
    render(<App />);
    await waitFor(() => expect(memberApi.getMembers).toHaveBeenCalled());

    await user.type(screen.getByLabelText(/name:/i), "Alice");
    await user.type(screen.getByLabelText(/email:/i), "alice@example.com");
    await user.type(screen.getByLabelText(/phone #:/i), "2125551212");
    await user.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() =>
      expect(screen.getByText("Internal server error")).toBeInTheDocument()
    );
  });
});
