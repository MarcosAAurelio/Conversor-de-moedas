try {
  const saved = localStorage.getItem("currency-theme");
  document.documentElement.dataset.theme =
    saved || (matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
  document.documentElement.lang =
    localStorage.getItem("currency-language") === "en" ? "en" : "pt-BR";
} catch {
  document.documentElement.dataset.theme = "light";
}
