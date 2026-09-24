const { test, expect } = require("@playwright/test");

test.beforeEach(async ({ page }) => {
  await page.route("**/api/cotacoes/historico**", (route) =>
    route.fulfill({ status: 200, contentType: "application/json", body: "[]" }),
  );
});

test("blocks an amount larger than the supported precision", async ({ page }) => {
  await page.goto("/");

  await expect(page.getByRole("heading", { name: "Converta suas moedas" })).toBeVisible();
  await page.getByLabel("Valor a converter").fill("10000000000000");
  await page.getByRole("button", { name: "Converter agora" }).click();

  await expect(page.getByRole("status").filter({ hasText: "Informe um valor maior que zero." })).toBeVisible();
  await expect(page).toHaveURL(/\/$/);
});

test("opens the global conversion history page", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Ver histórico completo" }).click();

  await expect(page.getByRole("heading", { name: "Histórico de conversões" })).toBeVisible();
  await expect(page.getByText("Seu histórico começa aqui")).toBeVisible();
});
