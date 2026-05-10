import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs';

mermaid.initialize({
  startOnLoad: false
});

document.addEventListener("DOMContentLoaded", async () => {

  const blocks = document.querySelectorAll(
    'code.language-mermaid'
  );

  for (const block of blocks) {

    const parent = block.parentElement;

    const container = document.createElement('div');
    container.className = 'mermaid';

    container.textContent = block.textContent;

    parent.replaceWith(container);
  }

  await mermaid.run();
});