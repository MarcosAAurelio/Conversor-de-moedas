const { isValidConversionAmount } = require("../../src/main/resources/static/js/currency-input.js");

describe("isValidConversionAmount", () => {
  test.each([
    undefined,
    null,
    "",
    " ",
    "abc",
    "1e3",
    "-1",
    "0",
    "0.000000",
    "0.0000001",
    "1.1234567",
  ])("rejects invalid or unsupported input %p", (value) => {
    expect(isValidConversionAmount(value)).toBe(false);
  });

  test.each(["0.01", "1", "1234567890123.123456", "0000000000000000001.25", " 42.5 "])(
    "accepts a positive supported amount %p",
    (value) => {
      expect(isValidConversionAmount(value)).toBe(true);
    },
  );

  test("rejects amounts above the storage precision", () => {
    expect(isValidConversionAmount("10000000000000")).toBe(false);
  });
});
