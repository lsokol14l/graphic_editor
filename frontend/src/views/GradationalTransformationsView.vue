<template>
  <main class="relative min-h-screen overflow-hidden">
    <div class="pointer-events-none absolute inset-0">
      <div
        class="absolute -top-16 right-0 h-72 w-72 rounded-full bg-emerald-400/20 blur-3xl"
      ></div>
      <div
        class="absolute bottom-0 left-0 h-72 w-72 rounded-full bg-cyan-500/15 blur-3xl"
      ></div>
    </div>

    <section
      class="relative mx-auto flex min-h-screen w-full max-w-6xl flex-col gap-6 px-4 py-8 md:px-8"
    >
      <header
        class="rounded-2xl border border-slate-700/70 bg-slate-900/75 p-6 shadow-soft backdrop-blur"
      >
        <RouterLink
          to="/"
          class="inline-flex items-center rounded-lg border border-slate-600 bg-slate-800/80 px-3 py-2 text-xs font-semibold uppercase tracking-[0.14em] text-slate-300 transition hover:border-slate-500 hover:text-white"
        >
          Назад
        </RouterLink>
        <p class="mt-4 text-xs uppercase tracking-[0.2em] text-emerald-300">
          Image Lab
        </p>
        <h1 class="mt-2 text-3xl font-bold text-white md:text-4xl">
          Gradational transformations
        </h1>
        <p class="mt-2 max-w-2xl text-sm text-slate-300">
          Каркас модуля готов. Здесь можно добавить гистограммы,
          контрастирование и нелинейные преобразования.
        </p>
      </header>

      <div class="grid gap-4 md:grid-cols-3">
        <article
          class="rounded-2xl border border-slate-700/80 bg-slate-900/80 p-5"
        >
          <h2 class="text-base font-semibold text-white">Histogram tools</h2>
          <p class="mt-2 text-sm text-slate-300">
            Анализ распределения яркости и канальных гистограмм.
          </p>
        </article>
        <article
          class="rounded-2xl border border-slate-700/80 bg-slate-900/80 p-5"
        >
          <h2 class="text-base font-semibold text-white">Linear transforms</h2>
          <p class="mt-2 text-sm text-slate-300">
            Настройка яркости, контраста и пороговых преобразований.
          </p>
        </article>
        <article
          class="rounded-2xl border border-slate-700/80 bg-slate-900/80 p-5"
        >
          <h2 class="text-base font-semibold text-white">
            Non-linear transforms
          </h2>
          <p class="mt-2 text-sm text-slate-300">
            Гамма-коррекция, логарифмирование и степенные функции.
          </p>
        </article>
      </div>

      <div
        class="rounded-2xl border border-dashed border-emerald-500/40 bg-emerald-500/10 p-6 text-sm text-emerald-100"
      >
        TODO: добавить API-интеграцию, форму загрузки изображения и предпросмотр
        результата для градационных преобразований.
      </div>
    </section>
  </main>
</template>
