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

  await expect(
    page.getByRole("status").filter({ hasText: "Informe um valor maior que zero." }),
  ).toBeVisible();
  await expect(page).toHaveURL(/\/$/);
});

test("opens the global conversion history page", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Ver histórico completo" }).click();

  await expect(page.getByRole("heading", { name: "Histórico de conversões" })).toBeVisible();
  await expect(page.getByText("Seu histórico começa aqui")).toBeVisible();
});

test("applies the tilt effect to the three cards on the about page", async ({ page }) => {
  await page.goto("/sobre");

  const cards = page.locator("[data-tilt-card]");
  await expect(cards).toHaveCount(3);

  const profileCard = cards.first();
  await profileCard.hover({ position: { x: 20, y: 20 } });
  await expect(profileCard).toHaveAttribute("data-tilt-active", "true");
  await expect
    .poll(() => profileCard.evaluate((card) => card.style.getPropertyValue("--tilt-rotation-y")))
    .not.toBe("0deg");
});

test("shows the updated about heading in both languages", async ({ page }) => {
  await page.goto("/sobre");
  const heading = page.locator("#aboutTitle");

  await expect(heading).toHaveText("Meu nome é Marcos Aurélio.");
  await page.getByRole("button", { name: "English" }).click();
  await expect(heading).toHaveText("My name is Marcos Aurélio.");
  await page.getByRole("button", { name: "Português" }).click();
  await expect(heading).toHaveText("Meu nome é Marcos Aurélio.");
});

test("disables the about card tilt when reduced motion is requested", async ({ page }) => {
  await page.emulateMedia({ reducedMotion: "reduce" });
  await page.goto("/sobre");

  const profileCard = page.locator("[data-tilt-card]").first();
  await profileCard.hover({ position: { x: 20, y: 20 } });

  await expect(profileCard).not.toHaveAttribute("data-tilt-active", "true");
  await expect
    .poll(() => profileCard.evaluate((card) => card.style.getPropertyValue("--tilt-rotation-y")))
    .toBe("");
});
