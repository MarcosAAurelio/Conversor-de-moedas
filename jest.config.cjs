module.exports = {
  testMatch: ["<rootDir>/tests/unit/**/*.test.cjs"],
  modulePathIgnorePatterns: ["<rootDir>/.stryker-tmp/", "<rootDir>/stryker-tmp/"],
  collectCoverageFrom: ["src/main/resources/static/js/currency-input.js"],
  coverageDirectory: "coverage",
  coverageReporters: ["lcov", "text"],
};
