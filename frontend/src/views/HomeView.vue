<template>
  <main class="relative min-h-screen overflow-hidden">
    <div class="pointer-events-none absolute inset-0">
      <div
          class="absolute -top-20 left-0 h-80 w-80 rounded-full bg-cyan-400/20 blur-3xl"
      ></div>
      <div
          class="absolute right-0 top-20 h-80 w-80 rounded-full bg-emerald-500/20 blur-3xl"
      ></div>
      <div
          class="absolute bottom-0 left-1/4 h-72 w-72 rounded-full bg-orange-400/15 blur-3xl"
      ></div>
    </div>

    <section
        class="relative mx-auto flex min-h-screen w-full max-w-6xl flex-col justify-center px-4 py-10 md:px-8"
    >
      <header
          class="mb-8 rounded-2xl border border-slate-700/70 bg-slate-900/75 p-6 shadow-soft backdrop-blur"
      >
        <h1 class="mt-3 text-3xl font-bold text-cyan-300 md:text-5xl">
          Graphic Editor
        </h1>
      </header>

      <div class="grid gap-4 md:grid-cols-2">
        <RouterLink
            to="/image-redactor"
            class="group rounded-2xl border border-cyan-500/40 bg-slate-900/80 p-6 transition hover:-translate-y-1 hover:border-cyan-300 hover:bg-slate-900"
        >
          <p class="text-xs uppercase tracking-[0.2em] text-cyan-300">
            Module 01
          </p>
          <h2 class="mt-3 text-2xl font-bold text-white">Image redactor</h2>
          <p class="mt-2 text-sm text-slate-300">
            Текущий рабочий редактор изображений: одиночные операции, маски и
            режим слоев.
          </p>
          <span
              class="mt-4 inline-flex items-center text-sm font-semibold text-cyan-200"
          >Открыть модуль</span
          >
        </RouterLink>

        <RouterLink
            to="/gradational-transformations"
            class="group rounded-2xl border border-emerald-500/40 bg-slate-900/80 p-6 transition hover:-translate-y-1 hover:border-emerald-300 hover:bg-slate-900"
        >
          <p class="text-xs uppercase tracking-[0.2em] text-emerald-300">
            Module 02
          </p>
          <h2 class="mt-3 text-2xl font-bold text-white">
            Gradational transformations
          </h2>
          <p class="mt-2 text-sm text-slate-300">
            Подготовленный модуль под дальнейшую реализацию градационных
            преобразований.
          </p>
          <span
              class="mt-4 inline-flex items-center text-sm font-semibold text-emerald-200"
          >Открыть модуль</span
          >
        </RouterLink>
      </div>
    </section>
  </main>
</template>
