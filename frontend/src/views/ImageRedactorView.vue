<script setup>
import { computed, ref } from 'vue'

const mode = ref('SINGLE')

const operation = ref('SUM')
const channels = ref('RGB')
const maskShape = ref('CIRCLE')
const image1 = ref(null)
const image2 = ref(null)
const firstInputRef = ref(null)
const secondInputRef = ref(null)

const layers = ref([])
const layersInputRef = ref(null)

const resultUrl = ref('')
const errorText = ref('')
const busy = ref(false)

const needsSecondImage = computed(() => operation.value !== 'MASK')

const blendModes = [
  'NONE',
  'SUM',
  'DIFFERENCE',
  'MULTIPLY',
  'AVERAGE',
  'MINIMUM',
  'MAXIMUM'
]

function clearResult() {
  if (resultUrl.value) {
    URL.revokeObjectURL(resultUrl.value)
  }
  resultUrl.value = ''
}

function onFileChange(event, target) {
  const file = event.target.files?.[0] || null
  if (target === 1) {
    image1.value = file
  } else {
    image2.value = file
  }
}

function openFileDialog(target) {
  if (target === 1 && firstInputRef.value) {
    firstInputRef.value.click()
    return
  }

  if (target === 2 && secondInputRef.value && needsSecondImage.value) {
    secondInputRef.value.click()
  }
}

async function processSingle() {
  errorText.value = ''
  if (!image1.value) {
    errorText.value = 'Выберите первое изображение.'
    return
  }
  if (needsSecondImage.value && !image2.value) {
    errorText.value = 'Для этой операции нужно второе изображение.'
    return
  }

  const formData = new FormData()
  formData.append('image1', image1.value)
  if (image2.value) {
    formData.append('image2', image2.value)
  }
  formData.append('operation', operation.value)
  formData.append('channels', channels.value)
  formData.append('maskShape', maskShape.value)

  busy.value = true
  try {
    const response = await fetch('http://localhost:8080/api/images/process', {
      method: 'POST',
      body: formData
    })

    if (!response.ok) {
      const text = await response.text()
      throw new Error(text || 'Не удалось обработать изображения')
    }

    const blob = await response.blob()
    clearResult()
    resultUrl.value = URL.createObjectURL(blob)
  } catch (error) {
    errorText.value = error.message
  } finally {
    busy.value = false
  }
}

function addLayers(event) {
  const files = Array.from(event.target.files || [])
  for (const file of files) {
    layers.value.push({
      id: crypto.randomUUID(),
      file,
      name: file.name,
      mode: 'NONE',
      opacity: 100
    })
  }
  event.target.value = ''
}

function openLayersDialog() {
  if (layersInputRef.value) {
    layersInputRef.value.click()
  }
}

function removeLayer(id) {
  layers.value = layers.value.filter(layer => layer.id !== id)
}

function opacityPercent(layer) {
  return `${layer.opacity}%`
}

async function composeLayers() {
  errorText.value = ''
  if (layers.value.length === 0) {
    errorText.value = 'Добавьте хотя бы один слой.'
    return
  }

  const formData = new FormData()
  for (const layer of layers.value) {
    formData.append('images', layer.file)
    formData.append('modes', layer.mode)
    formData.append('opacities', String(layer.opacity / 100))
  }

  busy.value = true
  try {
    const response = await fetch('http://localhost:8080/api/images/compose', {
      method: 'POST',
      body: formData
    })

    if (!response.ok) {
      const text = await response.text()
      throw new Error(text || 'Не удалось собрать изображение')
    }

    const blob = await response.blob()
    clearResult()
    resultUrl.value = URL.createObjectURL(blob)
  } catch (error) {
    errorText.value = error.message
  } finally {
    busy.value = false
  }
}

function resetResult() {
  clearResult()
  errorText.value = ''
  image1.value = null
  image2.value = null
  layers.value = []

  if (firstInputRef.value) {
    firstInputRef.value.value = ''
  }
  if (secondInputRef.value) {
    secondInputRef.value.value = ''
  }
  if (layersInputRef.value) {
    layersInputRef.value.value = ''
  }
}
</script>

<template>
  <main class="relative min-h-screen overflow-hidden">
    <div class="pointer-events-none absolute inset-0">
      <div
        class="absolute -top-24 -left-24 h-72 w-72 rounded-full bg-cyan-400/20 blur-3xl"
      ></div>
      <div
        class="absolute top-16 right-0 h-80 w-80 rounded-full bg-orange-500/20 blur-3xl"
      ></div>
      <div
        class="absolute bottom-0 left-1/3 h-72 w-72 rounded-full bg-emerald-400/15 blur-3xl"
      ></div>
    </div>

    <div
      class="relative mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-8 md:px-8"
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
        <p class="mt-4 text-xs uppercase tracking-[0.2em] text-cyan-300">
          Image Lab
        </p>
        <h1 class="mt-2 text-3xl font-bold text-white md:text-4xl">
          Image redactor
        </h1>
        <p class="mt-2 max-w-2xl text-sm text-slate-300">
          Используйте обычные операции с каналами и маской или режим слоев, где
          композиция собирается снизу вверх.
        </p>
      </header>

      <div class="flex flex-wrap items-center gap-2">
        <button
          type="button"
          class="rounded-lg px-4 py-2 text-sm font-semibold transition"
          :class="
            mode === 'SINGLE'
              ? 'bg-cyan-500 text-white'
              : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
          "
          @click="mode = 'SINGLE'"
        >
          Обычная обработка
        </button>
        <button
          type="button"
          class="rounded-lg px-4 py-2 text-sm font-semibold transition"
          :class="
            mode === 'LAYERS'
              ? 'bg-cyan-500 text-white'
              : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
          "
          @click="mode = 'LAYERS'"
        >
          Слои
        </button>
      </div>

      <section class="grid gap-6 lg:grid-cols-[1.1fr_1fr]">
        <div
          class="rounded-2xl border border-slate-700/70 bg-slate-900/80 p-6 shadow-soft backdrop-blur"
        >
          <div v-if="mode === 'SINGLE'" class="grid gap-4 sm:grid-cols-2">
            <label class="field sm:col-span-2">
              <span class="field-label">Первое изображение</span>
              <input
                class="hidden"
                type="file"
                accept="image/*"
                ref="firstInputRef"
                @change="onFileChange($event, 1)"
              />
              <div
                class="rounded-xl border border-slate-700 bg-slate-900/80 p-3"
              >
                <div class="flex items-center justify-between gap-3">
                  <button
                    type="button"
                    class="inline-flex items-center rounded-lg border border-cyan-400/40 bg-cyan-500/15 px-3 py-2 text-sm font-medium text-cyan-200 transition hover:bg-cyan-500/25"
                    @click="openFileDialog(1)"
                  >
                    Выбрать файл
                  </button>
                  <span class="truncate text-sm text-slate-300">{{
                    image1 ? image1.name : 'Файл не выбран'
                  }}</span>
                </div>
              </div>
            </label>

            <label class="field sm:col-span-2">
              <span class="field-label">Второе изображение</span>
              <input
                class="hidden"
                type="file"
                accept="image/*"
                ref="secondInputRef"
                :disabled="!needsSecondImage"
                @change="onFileChange($event, 2)"
              />
              <div
                class="rounded-xl border border-slate-700 bg-slate-900/80 p-3"
                :class="{ 'opacity-60': !needsSecondImage }"
              >
                <div class="flex items-center justify-between gap-3">
                  <button
                    type="button"
                    class="inline-flex items-center rounded-lg border border-cyan-400/40 bg-cyan-500/15 px-3 py-2 text-sm font-medium text-cyan-200 transition hover:bg-cyan-500/25 disabled:cursor-not-allowed disabled:opacity-50"
                    :disabled="!needsSecondImage"
                    @click="openFileDialog(2)"
                  >
                    Выбрать файл
                  </button>
                  <span class="truncate text-sm text-slate-300">{{
                    image2 ? image2.name : 'Файл не выбран'
                  }}</span>
                </div>
              </div>
            </label>

            <label class="field">
              <span class="field-label">Операция</span>
              <select class="field-input" v-model="operation">
                <option value="SUM">Сумма</option>
                <option value="MULTIPLY">Произведение</option>
                <option value="AVERAGE">Среднее</option>
                <option value="MINIMUM">Минимум</option>
                <option value="MAXIMUM">Максимум</option>
                <option value="MASK">Маска</option>
              </select>
            </label>

            <label class="field">
              <span class="field-label">Каналы</span>
              <select class="field-input" v-model="channels">
                <option value="RGB">RGB</option>
                <option value="R">R</option>
                <option value="G">G</option>
                <option value="B">B</option>
                <option value="RG">RG</option>
                <option value="GB">GB</option>
                <option value="RB">RB</option>
              </select>
            </label>

            <label class="field sm:col-span-2">
              <span class="field-label">Форма маски</span>
              <select
                class="field-input"
                v-model="maskShape"
                :disabled="operation !== 'MASK'"
              >
                <option value="CIRCLE">Круг</option>
                <option value="SQUARE">Квадрат</option>
                <option value="RECTANGLE">Прямоугольник</option>
              </select>
            </label>
          </div>

          <div v-else class="space-y-3">
            <div class="mb-4 flex flex-wrap items-center gap-2">
              <input
                ref="layersInputRef"
                class="hidden"
                type="file"
                accept="image/*"
                multiple
                @change="addLayers"
              />
              <button
                type="button"
                class="inline-flex items-center rounded-lg border border-cyan-400/40 bg-cyan-500/15 px-4 py-2 text-sm font-medium text-cyan-200 transition hover:bg-cyan-500/25"
                @click="openLayersDialog"
              >
                Добавить изображения
              </button>
              <span class="text-sm text-slate-400"
                >Порядок в списке: снизу вверх</span
              >
            </div>

            <div
              v-for="(layer, index) in layers"
              :key="layer.id"
              class="rounded-xl border border-slate-700 bg-slate-900/70 p-3"
            >
              <div class="mb-2 flex items-center justify-between gap-2">
                <div class="min-w-0">
                  <p class="truncate text-sm font-medium text-slate-100">
                    {{ index + 1 }}. {{ layer.name }}
                  </p>
                  <p class="text-xs text-slate-400">
                    {{ index === 0 ? 'Базовый слой' : 'Накладываемый слой' }}
                  </p>
                </div>
                <button
                  type="button"
                  class="rounded-md border border-rose-500/40 bg-rose-500/15 px-2.5 py-1 text-xs text-rose-200 transition hover:bg-rose-500/30"
                  @click="removeLayer(layer.id)"
                >
                  Удалить
                </button>
              </div>

              <div class="grid gap-3 sm:grid-cols-2">
                <label class="field">
                  <span class="field-label">Метод наложения</span>
                  <select class="field-input" v-model="layer.mode">
                    <option
                      v-for="blendMode in blendModes"
                      :key="blendMode"
                      :value="blendMode"
                    >
                      {{ blendMode }}
                    </option>
                  </select>
                </label>

                <label class="field">
                  <span class="field-label"
                    >Прозрачность: {{ opacityPercent(layer) }}</span
                  >
                  <input
                    class="field-input"
                    type="range"
                    min="0"
                    max="100"
                    step="1"
                    v-model.number="layer.opacity"
                  />
                </label>
              </div>
            </div>

            <div
              v-if="layers.length === 0"
              class="rounded-xl border border-dashed border-slate-700 p-6 text-center text-sm text-slate-400"
            >
              Пока нет слоев. Добавьте изображения.
            </div>
          </div>

          <div class="mt-6 flex flex-wrap items-center gap-3">
            <button
              class="rounded-xl bg-gradient-to-r from-cyan-500 to-teal-500 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-cyan-500/25 transition hover:scale-[1.02] hover:from-cyan-400 hover:to-teal-400 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="busy"
              @click="mode === 'SINGLE' ? processSingle() : composeLayers()"
            >
              {{
                busy
                  ? 'Обработка...'
                  : mode === 'SINGLE'
                    ? 'Выполнить'
                    : 'Собрать изображение'
              }}
            </button>
            <p v-if="errorText" class="text-sm font-medium text-rose-300">
              {{ errorText }}
            </p>
          </div>
        </div>

        <div
          class="rounded-2xl border border-slate-700/70 bg-slate-900/80 p-6 shadow-soft backdrop-blur"
        >
          <h2 class="text-lg font-semibold text-white">Результат</h2>
          <p class="mt-1 text-sm text-slate-300">
            После обработки здесь появится итоговое изображение.
          </p>

          <div v-if="resultUrl" class="mt-4">
            <img
              class="max-h-[560px] w-full rounded-xl border border-slate-700 object-contain"
              :src="resultUrl"
              alt="Результат обработки"
            />
            <div class="mt-3 flex flex-wrap items-center justify-between gap-2">
              <a
                class="inline-flex items-center rounded-lg border border-cyan-400/40 bg-cyan-400/10 px-4 py-2 text-sm font-medium text-cyan-200 transition hover:bg-cyan-400/20"
                :href="resultUrl"
                download="result.png"
              >
                Скачать result.png
              </a>
              <button
                type="button"
                class="inline-flex items-center rounded-lg border border-rose-400/40 bg-rose-500/20 px-4 py-2 text-sm font-semibold text-rose-200 transition hover:bg-rose-500/35"
                @click="resetResult"
              >
                Сбросить
              </button>
            </div>
          </div>

          <div
            v-else
            class="mt-4 rounded-xl border border-dashed border-slate-700 p-8 text-center text-sm text-slate-400"
          >
            Результат пока не получен
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
