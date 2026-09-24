const { defineConfig } = require("@playwright/test");

const wrapperCommand = process.platform === "win32"
  ? ".\\mvnw.cmd -q spring-boot:run"
  : "./mvnw -q spring-boot:run";

module.exports = defineConfig({
  testDir: "./tests/e2e",
  fullyParallel: false,
  reporter: "list",
  use: {
    baseURL: "http://127.0.0.1:8080",
    browserName: "chromium",
    trace: "retain-on-failure",
  },
  webServer: {
    command: wrapperCommand,
    url: "http://127.0.0.1:8080/actuator/health",
    timeout: 120_000,
    reuseExistingServer: !process.env.CI,
    env: {
      ...process.env,
      SERVER_ADDRESS: "127.0.0.1",
      H2_CONSOLE_ENABLED: "false",
    },
  },
});
