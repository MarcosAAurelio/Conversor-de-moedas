(() => {
  const cards = document.querySelectorAll("[data-tilt-card]");
  const supportsHover = window.matchMedia("(hover: hover) and (pointer: fine)").matches;
  const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  if (cards.length === 0 || !supportsHover || prefersReducedMotion) {
    return;
  }

  const options = Object.freeze({
    tiltLimit: 15,
    scale: 1.05,
    perspective: 1200,
    effect: "evade",
    spotlight: true,
  });

  const updateCard = (card, event) => {
    const bounds = card.getBoundingClientRect();
    if (bounds.width === 0 || bounds.height === 0) {
      return;
    }

    const pointerX = Math.min(1, Math.max(0, (event.clientX - bounds.left) / bounds.width));
    const pointerY = Math.min(1, Math.max(0, (event.clientY - bounds.top) / bounds.height));
    const direction = options.effect === "evade" ? -1 : 1;
    const horizontalOffset = (pointerX - 0.5) * 2 * direction;
    const verticalOffset = (pointerY - 0.5) * 2 * direction;

    card.style.setProperty(
      "--tilt-rotation-x",
      `${(verticalOffset * options.tiltLimit).toFixed(2)}deg`,
    );
    card.style.setProperty(
      "--tilt-rotation-y",
      `${(horizontalOffset * options.tiltLimit).toFixed(2)}deg`,
    );
    card.style.setProperty("--tilt-scale", String(options.scale));
    card.style.setProperty("--tilt-perspective", `${options.perspective}px`);
    card.style.setProperty("--tilt-spot-x", `${(pointerX * 100).toFixed(2)}%`);
    card.style.setProperty("--tilt-spot-y", `${(pointerY * 100).toFixed(2)}%`);
    card.dataset.tiltActive = "true";
  };

  const resetCard = (card) => {
    card.removeAttribute("data-tilt-active");
    card.style.removeProperty("--tilt-rotation-x");
    card.style.removeProperty("--tilt-rotation-y");
    card.style.removeProperty("--tilt-scale");
    card.style.removeProperty("--tilt-perspective");
    card.style.removeProperty("--tilt-spot-x");
    card.style.removeProperty("--tilt-spot-y");
  };

  cards.forEach((card) => {
    card.addEventListener("pointermove", (event) => updateCard(card, event));
    card.addEventListener("pointerleave", () => resetCard(card));
    card.addEventListener("pointercancel", () => resetCard(card));
  });
})();
