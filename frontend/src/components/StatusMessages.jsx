import React from "react";

export default function StatusMessages({ globalMessage, globalError }) {
  return (
    <div className="messages">
      {globalMessage && <div className="valid">{globalMessage}</div>}
      {globalError && <div className="invalid">{globalError}</div>}
    </div>
  );
}
