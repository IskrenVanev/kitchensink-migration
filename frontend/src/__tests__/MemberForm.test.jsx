import React from "react";
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import MemberForm from "../components/MemberForm";

function renderForm(overrides = {}) {
  const props = {
    form: { name: "", email: "", phoneNumber: "" },
    fieldErrors: {},
    globalMessage: "",
    globalError: "",
    onInputChange: vi.fn(),
    onSubmit: vi.fn((e) => e.preventDefault()),
    ...overrides,
  };
  render(<MemberForm {...props} />);
  return props;
}

describe("MemberForm", () => {
  it("renders the Member Registration heading", () => {
    renderForm();
    expect(screen.getByText("Member Registration")).toBeInTheDocument();
  });

  it("renders name, email, and phone number fields", () => {
    renderForm();
    expect(screen.getByLabelText(/name:/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email:/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/phone #:/i)).toBeInTheDocument();
  });

  it("renders the Register button", () => {
    renderForm();
    expect(screen.getByRole("button", { name: /register/i })).toBeInTheDocument();
  });

  it("displays current form values in inputs", () => {
    renderForm({
      form: { name: "Alice", email: "alice@example.com", phoneNumber: "2125551212" },
    });
    expect(screen.getByLabelText(/name:/i)).toHaveValue("Alice");
    expect(screen.getByLabelText(/email:/i)).toHaveValue("alice@example.com");
    expect(screen.getByLabelText(/phone #:/i)).toHaveValue("2125551212");
  });

  it("calls onInputChange when user types in the name field", async () => {
    const user = userEvent.setup();
    const props = renderForm();
    await user.type(screen.getByLabelText(/name:/i), "Alice");
    expect(props.onInputChange).toHaveBeenCalled();
  });

  it("calls onInputChange when user types in the email field", async () => {
    const user = userEvent.setup();
    const props = renderForm();
    await user.type(screen.getByLabelText(/email:/i), "alice@example.com");
    expect(props.onInputChange).toHaveBeenCalled();
  });

  it("calls onInputChange when user types in the phone number field", async () => {
    const user = userEvent.setup();
    const props = renderForm();
    await user.type(screen.getByLabelText(/phone #:/i), "2125551212");
    expect(props.onInputChange).toHaveBeenCalled();
  });

  it("calls onSubmit when the Register button is clicked", async () => {
    const user = userEvent.setup();
    const props = renderForm();
    await user.click(screen.getByRole("button", { name: /register/i }));
    expect(props.onSubmit).toHaveBeenCalledOnce();
  });

  it("displays a field error for name when provided", () => {
    renderForm({ fieldErrors: { name: "Must not contain numbers" } });
    expect(screen.getByText("Must not contain numbers")).toBeInTheDocument();
  });

  it("displays a field error for email when provided", () => {
    renderForm({ fieldErrors: { email: "must be a well-formed email address" } });
    expect(screen.getByText("must be a well-formed email address")).toBeInTheDocument();
  });

  it("displays a field error for phoneNumber when provided", () => {
    renderForm({ fieldErrors: { phoneNumber: "size must be between 10 and 12" } });
    expect(screen.getByText("size must be between 10 and 12")).toBeInTheDocument();
  });

  it("displays multiple field errors simultaneously", () => {
    renderForm({
      fieldErrors: {
        name: "Must not contain numbers",
        email: "must be a well-formed email address",
      },
    });
    expect(screen.getByText("Must not contain numbers")).toBeInTheDocument();
    expect(screen.getByText("must be a well-formed email address")).toBeInTheDocument();
  });

  it("displays global success message via StatusMessages", () => {
    renderForm({ globalMessage: "Member registered successfully." });
    expect(screen.getByText("Member registered successfully.")).toBeInTheDocument();
  });

  it("displays global error message via StatusMessages", () => {
    renderForm({ globalError: "Registration failed" });
    expect(screen.getByText("Registration failed")).toBeInTheDocument();
  });

  it("does not display field error spans when fieldErrors is empty", () => {
    renderForm({ fieldErrors: {} });
    expect(screen.queryByRole("generic", { name: /invalid/i })).not.toBeInTheDocument();
  });

  it("does not display success or error messages when both are empty strings", () => {
    renderForm({ globalMessage: "", globalError: "" });
    expect(screen.queryByText("Member registered successfully.")).not.toBeInTheDocument();
    expect(screen.queryByText("Registration failed")).not.toBeInTheDocument();
  });
});
