((root, factory) => {
  const api = factory();

  // Stryker disable next-line ConditionalExpression,LogicalOperator: Jest and browser tests cover both runtime branches.
  if (typeof module === "object" && module.exports) {
    module.exports = api;
  } else {
    root.CurrencyInput = api;
  }
})(globalThis, () => {
  function isValidConversionAmount(rawValue) {
    if (typeof rawValue !== "string") return false;
    const value = rawValue.trim();
    if (!/^\d+(?:\.\d+)?$/.test(value)) return false;

    const [integerPart, fractionPart = ""] = value.split(".");
    const significantIntegerDigits = integerPart.replace(/^0+/, "").length;
    if (significantIntegerDigits > 13 || fractionPart.length > 6) return false;

    const numericValue = Number(value);
    return Number.isFinite(numericValue) && numericValue > 0;
  }

  return Object.freeze({ isValidConversionAmount });
});
