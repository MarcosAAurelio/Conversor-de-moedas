module.exports = {
  mutate: ["src/main/resources/static/js/currency-input.js"],
  testRunner: "jest",
  jest: {
    projectType: "custom",
    configFile: "jest.config.cjs",
  },
  coverageAnalysis: "perTest",
  tempDirName: "stryker-tmp",
  ignorePatterns: [".stryker-tmp", "stryker-tmp"],
  reporters: ["clear-text", "progress"],
  thresholds: {
    high: 90,
    low: 80,
    break: 70,
  },
  concurrency: 2,
};
