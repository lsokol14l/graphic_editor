<template>
  <main class="relative min-h-screen overflow-hidden bg-slate-950">
    <div class="pointer-events-none absolute inset-0">
      <div
        class="absolute -top-20 left-0 h-80 w-80 rounded-full bg-cyan-400/20 blur-3xl"
      ></div>
      <div
        class="absolute right-0 top-20 h-80 w-80 rounded-full bg-emerald-500/20 blur-3xl"
      ></div>
    </div>

    <div class="relative mx-auto w-full max-w-7xl px-4 py-6 md:px-8">
      <!-- Заголовок -->
      <header
        class="mb-6 rounded-2xl border border-slate-700/70 bg-slate-900/75 p-6 shadow-soft backdrop-blur flex justify-center"
      >
        <h1 class="text-3xl font-bold text-cyan-300 md:text-4xl">
          Histogram & Gradational Transformations
        </h1>
      </header>

      <!-- Основной контент -->
      <div class="space-y-6">
        <!-- Загрузка изображения -->
        <div
          class="rounded-2xl border border-cyan-500/40 bg-slate-900/80 p-6 shadow-soft"
        >
          <label class="block mb-4">
            <span class="text-sm font-medium text-cyan-300 mb-2 block"
              >Загрузить изображение</span
            >
            <input
              type="file"
              accept="image/*"
              @change="handleImageUpload"
              class="w-full px-3 py-2 rounded-xl border border-slate-700 bg-slate-900/90 text-slate-100 cursor-pointer hover:border-cyan-400 transition"
            />
          </label>
          <p
            v-if="imageStatus"
            :class="
              imageStatus.type === 'error'
                ? 'text-rose-400'
                : 'text-emerald-400'
            "
            class="text-sm"
          >
            {{ imageStatus.message }}
          </p>
        </div>

        <!-- Сетка с гистограммой и изображением -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- Левая часть: Гистограмма -->
          <div
            class="rounded-2xl border border-cyan-500/40 bg-slate-900/80 p-6 shadow-soft flex flex-col"
          >
            <h2 class="text-lg font-semibold text-cyan-300 mb-4">
              Распределение интенсивности
            </h2>
            <div class="flex-1 bg-white rounded-lg overflow-hidden min-h-64">
              <canvas ref="histogramCanvas" class="w-full h-full"></canvas>
            </div>
          </div>

          <!-- Правая часть: Исходное изображение -->
          <div
            class="rounded-2xl border border-emerald-500/40 bg-slate-900/80 p-6 shadow-soft flex flex-col"
          >
            <h2 class="text-lg font-semibold text-emerald-300 mb-4">
              Исходное изображение
            </h2>
            <div
              class="flex-1 bg-slate-800 rounded-lg overflow-hidden min-h-64 flex items-center justify-center"
            >
              <img
                v-if="originalImageSrc"
                :src="originalImageSrc"
                alt="Исходное изображение"
                class="max-w-full max-h-full object-contain"
              />
              <p v-else class="text-slate-400 text-center">
                Загрузите изображение
              </p>
            </div>
          </div>
        </div>

        <!-- Нижняя часть: Гистограмма значений серого -->
        <div
          class="rounded-2xl border border-orange-500/40 bg-slate-900/80 p-6 shadow-soft"
        >
          <h2 class="text-lg font-semibold text-orange-300 mb-4">
            График распределения пикселей
          </h2>
          <div class="bg-slate-950 rounded-lg overflow-hidden h-48">
            <canvas ref="intensityCanvas" class="w-full h-full"></canvas>
          </div>
        </div>

        <!-- Статистика -->
        <div
          v-if="histogramData"
          class="rounded-2xl border border-slate-700/70 bg-slate-900/75 p-6 shadow-soft"
        >
          <h2 class="text-lg font-semibold text-slate-200 mb-4">Статистика</h2>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div class="bg-slate-800/50 rounded-lg p-4 text-center">
              <p class="text-xs text-slate-400">Min интенсивность</p>
              <p class="text-xl font-bold text-cyan-300">
                {{ histogramData.min }}
              </p>
            </div>
            <div class="bg-slate-800/50 rounded-lg p-4 text-center">
              <p class="text-xs text-slate-400">Max интенсивность</p>
              <p class="text-xl font-bold text-emerald-300">
                {{ histogramData.max }}
              </p>
            </div>
            <div class="bg-slate-800/50 rounded-lg p-4 text-center">
              <p class="text-xs text-slate-400">Средняя интенсивность</p>
              <p class="text-xl font-bold text-orange-300">
                {{ histogramData.mean }}
              </p>
            </div>
            <div class="bg-slate-800/50 rounded-lg p-4 text-center">
              <p class="text-xs text-slate-400">Дисперсия</p>
              <p class="text-xl font-bold text-pink-300">
                {{ histogramData.variance }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
export default {
  name: 'HistogramView',
  data() {
    return {
      originalImageSrc: null,
      histogramData: null,
      imageStatus: null,
      originalImage: null
    }
  },
  methods: {
    async handleImageUpload(event) {
      const file = event.target.files?.[0]
      if (!file) {
        this.imageStatus = { type: 'error', message: 'Выберите файл' }
        return
      }

      if (!file.type.startsWith('image/')) {
        this.imageStatus = {
          type: 'error',
          message: 'Выберите изображение (JPG, PNG и т.д.)'
        }
        return
      }

      // Показываем исходное изображение
      const reader = new FileReader()
      reader.onload = e => {
        this.originalImageSrc = e.target.result

        // Загружаем изображение для обработки
        const img = new Image()
        img.onload = () => {
          this.originalImage = img
          this.calculateHistogram()
        }
        img.src = e.target.result
      }
      reader.readAsDataURL(file)

      this.imageStatus = { type: 'success', message: 'Изображение загружено' }
    },

    calculateHistogram() {
      if (!this.originalImage) return

      // Создаём canvas для получения пиксельных данных
      const canvas = document.createElement('canvas')
      canvas.width = this.originalImage.width
      canvas.height = this.originalImage.height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(this.originalImage, 0, 0)

      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      const data = imageData.data

      // Вычисляем гистограмму (256 значений от 0 до 255)
      const histogram = new Array(256).fill(0)
      let sum = 0
      let sumSquares = 0
      let min = 255
      let max = 0

      // Преобразуем RGB в оттенки серого
      for (let i = 0; i < data.length; i += 4) {
        const r = data[i]
        const g = data[i + 1]
        const b = data[i + 2]

        // Формула преобразования RGB в серый: Y = 0.299*R + 0.587*G + 0.114*B
        const gray = Math.round(0.299 * r + 0.587 * g + 0.114 * b)

        histogram[gray]++
        sum += gray
        sumSquares += gray * gray

        if (gray < min) min = gray
        if (gray > max) max = gray
      }

      const pixelCount = data.length / 4
      const mean = Math.round(sum / pixelCount)
      const variance = Math.round(sumSquares / pixelCount - mean * mean)

      this.histogramData = {
        histogram,
        min,
        max,
        mean,
        variance
      }

      this.$nextTick(() => {
        this.drawHistogram()
        this.drawIntensityGraph()
      })
    },

    drawHistogram() {
      if (!this.histogramData) return

      const canvas = this.$refs.histogramCanvas
      if (!canvas) return

      const ctx = canvas.getContext('2d')
      const width = canvas.offsetWidth
      const height = canvas.offsetHeight

      canvas.width = width
      canvas.height = height

      const histogram = this.histogramData.histogram
      const maxValue = Math.max(...histogram)

      // Рисуем белый фон
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, width, height)

      // Рисуем гистограмму (красная кривая как в примере)
      ctx.strokeStyle = '#dc2626'
      ctx.fillStyle = 'rgba(220, 38, 38, 0.1)'
      ctx.lineWidth = 2

      const barWidth = width / 256
      ctx.beginPath()

      for (let i = 0; i < 256; i++) {
        const x = i * barWidth
        const normalizedValue = histogram[i] / maxValue
        const y = height - normalizedValue * (height * 0.9)

        if (i === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      }

      ctx.stroke()

      // Заливаем область под кривой
      ctx.lineTo(255 * barWidth, height)
      ctx.lineTo(0, height)
      ctx.closePath()
      ctx.fill()

      // Рисуем сетку и оси
      ctx.strokeStyle = '#e2e8f0'
      ctx.lineWidth = 0.5
      ctx.font = '10px sans-serif'
      ctx.fillStyle = '#64748b'

      // Вертикальные линии
      for (let i = 0; i <= 10; i++) {
        const x = (i / 10) * width
        ctx.beginPath()
        ctx.moveTo(x, 0)
        ctx.lineTo(x, height)
        ctx.stroke()

        if (i % 2 === 0) {
          ctx.fillText(Math.round((i / 10) * 255), x - 10, height + 10)
        }
      }

      // Горизонтальные линии
      for (let i = 0; i <= 5; i++) {
        const y = (i / 5) * height
        ctx.beginPath()
        ctx.moveTo(0, y)
        ctx.lineTo(width, y)
        ctx.stroke()
      }
    },

    drawIntensityGraph() {
      if (!this.histogramData) return

      const canvas = this.$refs.intensityCanvas
      if (!canvas) return

      const ctx = canvas.getContext('2d')
      const width = canvas.offsetWidth
      const height = canvas.offsetHeight

      canvas.width = width
      canvas.height = height

      const histogram = this.histogramData.histogram
      const maxValue = Math.max(...histogram)

      // Чёрный фон
      ctx.fillStyle = '#0f172a'
      ctx.fillRect(0, 0, width, height)

      // Рисуем гистограмму (чёрный график как внизу примера)
      ctx.fillStyle = '#1a1a1a'
      ctx.strokeStyle = '#1a1a1a'
      ctx.lineWidth = 1

      const barWidth = width / 256

      for (let i = 0; i < 256; i++) {
        const x = i * barWidth
        const normalizedValue = histogram[i] / maxValue
        const barHeight = normalizedValue * height

        ctx.fillRect(x, height - barHeight, barWidth, barHeight)
      }

      // Рисуем сетку
      ctx.strokeStyle = '#334155'
      ctx.lineWidth = 0.5

      for (let i = 0; i <= 4; i++) {
        const y = (i / 4) * height
        ctx.beginPath()
        ctx.moveTo(0, y)
        ctx.lineTo(width, y)
        ctx.stroke()
      }
    }
  }
}
</script>

<style scoped>
canvas {
  display: block;
}
</style>
