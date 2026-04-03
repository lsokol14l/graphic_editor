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
        <div class="flex w-full items-center justify-between">
          <RouterLink
            to="/"
            class="inline-flex items-center rounded-lg border border-slate-600 bg-slate-800/80 px-3 py-2 text-xs font-semibold uppercase tracking-[0.14em] text-slate-300 transition hover:border-slate-500 hover:text-white"
          >
            Назад
          </RouterLink>
          <h1 class="text-3xl font-bold text-cyan-300 md:text-4xl">
            Градационные преобразования
          </h1>
          <div class="w-[78px]"></div>
        </div>
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
              ref="fileInput"
              type="file"
              accept="image/*"
              @change="handleImageUpload"
              class="hidden"
            />
            <div
              class="flex items-center gap-3 rounded-xl border border-slate-700 bg-slate-900/90 px-3 py-2"
            >
              <button
                type="button"
                @click="openFileDialog"
                class="inline-flex items-center rounded-lg border border-cyan-500/60 bg-cyan-500/20 px-3 py-1.5 text-xs font-semibold uppercase tracking-[0.14em] text-cyan-200 transition hover:bg-cyan-500/30"
              >
                Выбрать файл
              </button>
              <span class="text-sm text-slate-300 truncate">
                {{ selectedFileName || 'Файл не выбран' }}
              </span>
            </div>
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

        <!-- Основная сетка: изображение и функция преобразования -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- Слева: Исходное изображение -->
          <div
            class="rounded-2xl border border-slate-700/70 bg-slate-900/80 p-6 shadow-soft flex flex-col"
          >
            <h2 class="text-lg font-semibold text-slate-300 mb-4">
              Изображение
            </h2>
            <div
              class="flex-1 bg-slate-800 rounded-lg overflow-hidden min-h-96 flex items-center justify-center"
            >
              <img
                v-if="processedImageSrc || originalImageSrc"
                :src="processedImageSrc || originalImageSrc"
                alt="Изображение"
                class="max-w-full max-h-full object-contain"
              />
              <p v-else class="text-slate-400 text-center">
                Загрузите изображение
              </p>
            </div>
            <div class="mt-4 flex items-center justify-between gap-3">
              <button
                @click="downloadImage"
                :disabled="!processedImageSrc && !originalImageSrc"
                class="inline-flex items-center rounded-lg border border-emerald-500/60 bg-emerald-600/25 px-3 py-2 text-xs font-semibold uppercase tracking-[0.14em] text-emerald-200 transition hover:bg-emerald-600/35 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Скачать
              </button>
              <button
                @click="resetImage"
                :disabled="!originalImageSrc && !processedImageSrc"
                class="inline-flex items-center rounded-lg border border-rose-500/60 bg-rose-600/25 px-3 py-2 text-xs font-semibold uppercase tracking-[0.14em] text-rose-200 transition hover:bg-rose-600/35 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Сбросить
              </button>
            </div>
          </div>

          <!-- Справа: Интерактивная кривая преобразования -->
          <div
            class="rounded-2xl border border-orange-500/40 bg-slate-900/80 p-6 shadow-soft flex flex-col"
          >
            <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 class="text-lg font-semibold text-orange-300">
                  Функция преобразования
                </h2>
                <p class="mt-1 text-xs text-slate-400">
                  Выберите способ интерполяции для кривой
                </p>
              </div>
              <div class="flex items-center gap-2">
                <select
                  v-model="interpolationMode"
                  class="rounded-lg border border-slate-600 bg-slate-900 px-3 py-2 text-sm text-slate-100 outline-none transition focus:border-orange-400"
                >
                  <option value="linear">Линейная</option>
                  <option value="cubic">Кубическая</option>
                </select>
                <button
                  @click="resetTransformationCurve"
                  class="px-3 py-1 rounded-lg bg-orange-600/40 hover:bg-orange-600/60 text-orange-300 text-sm transition"
                >
                  Сброс
                </button>
              </div>
            </div>
            <div
              class="flex-1 bg-white rounded-lg overflow-hidden min-h-96 cursor-crosshair"
              @click="drawOnCurve"
            >
              <canvas
                ref="transformationCanvas"
                class="w-full h-full cursor-crosshair"
              ></canvas>
            </div>
            <p class="mt-2 text-xs text-slate-400 text-center">
              Кликните для добавления точек
            </p>
          </div>
        </div>

        <!-- Гистограмма -->
        <div
          class="rounded-2xl border border-cyan-500/40 bg-slate-900/80 p-6 shadow-soft"
        >
          <h2 class="text-lg font-semibold text-cyan-300 mb-4">
            Гистограмма распределения интенсивности
          </h2>
          <div class="bg-white rounded-lg overflow-hidden h-56">
            <canvas ref="histogramCanvas" class="w-full h-full"></canvas>
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
              <p class="text-xs text-slate-400">Среднее значение</p>
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
      processedImageSrc: null,
      histogramData: null,
      selectedFileName: '',
      imageStatus: null,
      originalImage: null,
      transformationPoints: [],
      interpolationMode: 'linear',
      isProcessing: false
    }
  },
  watch: {
    transformationPoints: {
      handler() {
        if (this.originalImage) {
          this.applyTransformation()
        }
      },
      deep: true
    },
    interpolationMode() {
      if (this.originalImage) {
        this.drawTransformationCurve()
        this.applyTransformation()
      } else {
        this.drawTransformationCurve()
      }
    }
  },
  methods: {
    openFileDialog() {
      this.$refs.fileInput?.click()
    },

    async handleImageUpload(event) {
      const file = event.target.files?.[0]
      if (!file) {
        this.imageStatus = { type: 'error', message: 'Выберите файл' }
        return
      }

      this.processedImageSrc = null
      this.selectedFileName = file.name

      if (!file.type.startsWith('image/')) {
        this.imageStatus = {
          type: 'error',
          message: 'Выберите изображение (JPG, PNG и т.д.)'
        }
        return
      }

      const reader = new FileReader()
      reader.onload = e => {
        this.originalImageSrc = e.target.result
        const img = new Image()
        img.onload = () => {
          this.originalImage = img
          this.calculateHistogram()
          this.initTransformationCurve()
        }
        img.src = e.target.result
      }
      reader.readAsDataURL(file)

      this.imageStatus = { type: 'success', message: 'Изображение загружено' }
    },

    calculateHistogram() {
      if (!this.originalImage) return

      const canvas = document.createElement('canvas')
      canvas.width = this.originalImage.width
      canvas.height = this.originalImage.height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(this.originalImage, 0, 0)

      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      const data = imageData.data

      const histogram = new Array(256).fill(0)
      let sum = 0
      let sumSquares = 0
      let min = 255
      let max = 0

      for (let i = 0; i < data.length; i += 4) {
        const r = data[i]
        const g = data[i + 1]
        const b = data[i + 2]
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

      this.histogramData = { histogram, min, max, mean, variance }

      this.$nextTick(() => {
        this.drawHistogram()
      })
    },

    initTransformationCurve() {
      this.transformationPoints = [
        { x: 0, y: 0 },
        { x: 255, y: 255 }
      ]
      this.$nextTick(() => {
        this.drawTransformationCurve()
      })
    },

    drawOnCurve(event) {
      const canvas = this.$refs.transformationCanvas
      if (!canvas) return

      const rect = canvas.getBoundingClientRect()
      const x = event.clientX - rect.left
      const y = event.clientY - rect.top

      const width = canvas.offsetWidth
      const height = canvas.offsetHeight

      const grayValue = Math.round((x / width) * 255)
      const outputValue = Math.round(255 - (y / height) * 255)
      // Удаляем существующую точку с таким же x, если она есть
      const existingIndex = this.transformationPoints.findIndex(
        p => Math.abs(p.x - grayValue) < 5
      )
      if (existingIndex !== -1) {
        this.transformationPoints[existingIndex].y = outputValue
      } else {
        this.transformationPoints.push({ x: grayValue, y: outputValue })
      }
      this.transformationPoints.sort((a, b) => a.x - b.x)

      this.drawTransformationCurve()
    },

    drawTransformationCurve() {
      const canvas = this.$refs.transformationCanvas
      if (!canvas) return

      const ctx = canvas.getContext('2d')
      const width = canvas.offsetWidth
      const height = canvas.offsetHeight

      canvas.width = width
      canvas.height = height

      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, width, height)

      // Сетка
      ctx.strokeStyle = '#e0e0e0'
      ctx.lineWidth = 1
      for (let i = 0; i <= 5; i++) {
        const pos = (i / 5) * width
        ctx.beginPath()
        ctx.moveTo(pos, 0)
        ctx.lineTo(pos, height)
        ctx.stroke()

        const posY = (i / 5) * height
        ctx.beginPath()
        ctx.moveTo(0, posY)
        ctx.lineTo(width, posY)
        ctx.stroke()
      }

      // Диагональная опорная линия
      ctx.strokeStyle = '#cccccc'
      ctx.lineWidth = 1
      ctx.setLineDash([5, 5])
      ctx.beginPath()
      ctx.moveTo(0, height)
      ctx.lineTo(width, 0)
      ctx.stroke()
      ctx.setLineDash([])

      // Рисуем кривую преобразования
      if (this.transformationPoints.length > 1) {
        ctx.strokeStyle = '#dc2626'
        ctx.lineWidth = 3
        ctx.beginPath()

        for (let inputValue = 0; inputValue <= 255; inputValue++) {
          const outputValue = this.getTransformedValue(inputValue)
          const x = (inputValue / 255) * width
          const y = height - (outputValue / 255) * height

          if (inputValue === 0) {
            ctx.moveTo(x, y)
          } else {
            ctx.lineTo(x, y)
          }
        }

        ctx.stroke()

        // Заливка под кривой
        ctx.fillStyle = 'rgba(220, 38, 38, 0.1)'
        ctx.lineTo(width, height)
        ctx.lineTo(0, height)
        ctx.closePath()
        ctx.fill()
      }

      // Рисуем точки
      ctx.fillStyle = '#dc2626'
      for (const point of this.transformationPoints) {
        const x = (point.x / 255) * width
        const y = height - (point.y / 255) * height
        ctx.beginPath()
        ctx.arc(x, y, 5, 0, Math.PI * 2)
        ctx.fill()

        ctx.strokeStyle = '#ffffff'
        ctx.lineWidth = 2
        ctx.stroke()
      }
    },

    getTransformedValue(inputValue) {
      if (this.transformationPoints.length === 0) return inputValue

      if (
        this.interpolationMode === 'cubic' &&
        this.transformationPoints.length >= 3
      ) {
        return this.getCubicTransformedValue(inputValue)
      }

      let lower = this.transformationPoints[0]
      let upper =
        this.transformationPoints[this.transformationPoints.length - 1]

      for (let i = 0; i < this.transformationPoints.length - 1; i++) {
        if (
          this.transformationPoints[i].x <= inputValue &&
          inputValue <= this.transformationPoints[i + 1].x
        ) {
          lower = this.transformationPoints[i]
          upper = this.transformationPoints[i + 1]
          break
        }
      }

      const t = (inputValue - lower.x) / (upper.x - lower.x || 1)
      const outputValue = lower.y + t * (upper.y - lower.y)

      return Math.max(0, Math.min(255, Math.round(outputValue)))
    },

    getCubicTransformedValue(inputValue) {
      const points = this.transformationPoints
      const count = points.length

      if (count < 3) {
        return this.getTransformedValue(inputValue)
      }

      const x = points.map(point => point.x)
      const y = points.map(point => point.y)
      const h = []
      for (let i = 0; i < count - 1; i++) {
        h.push(Math.max(1, x[i + 1] - x[i]))
      }

      const alpha = new Array(count).fill(0)
      for (let i = 1; i < count - 1; i++) {
        alpha[i] =
          (3 / h[i]) * (y[i + 1] - y[i]) - (3 / h[i - 1]) * (y[i] - y[i - 1])
      }

      const l = new Array(count).fill(0)
      const mu = new Array(count).fill(0)
      const z = new Array(count).fill(0)
      const c = new Array(count).fill(0)
      const b = new Array(count - 1).fill(0)
      const d = new Array(count - 1).fill(0)

      l[0] = 1
      mu[0] = 0
      z[0] = 0

      for (let i = 1; i < count - 1; i++) {
        l[i] = 2 * (x[i + 1] - x[i - 1]) - h[i - 1] * mu[i - 1]
        if (l[i] === 0) l[i] = 1
        mu[i] = h[i] / l[i]
        z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i]
      }

      l[count - 1] = 1
      z[count - 1] = 0
      c[count - 1] = 0

      for (let j = count - 2; j >= 0; j--) {
        c[j] = z[j] - mu[j] * c[j + 1]
        b[j] = (y[j + 1] - y[j]) / h[j] - (h[j] * (c[j + 1] + 2 * c[j])) / 3
        d[j] = (c[j + 1] - c[j]) / (3 * h[j])
      }

      let interval = count - 2
      for (let i = 0; i < count - 1; i++) {
        if (inputValue <= x[i + 1]) {
          interval = i
          break
        }
      }

      const deltaX = inputValue - x[interval]
      const outputValue =
        y[interval] +
        b[interval] * deltaX +
        c[interval] * deltaX * deltaX +
        d[interval] * deltaX * deltaX * deltaX

      return Math.max(0, Math.min(255, Math.round(outputValue)))
    },

    resetTransformationCurve() {
      this.initTransformationCurve()
    },

    downloadImage() {
      const imageUrl = this.processedImageSrc || this.originalImageSrc
      if (!imageUrl) return

      const link = document.createElement('a')
      link.href = imageUrl
      link.download = 'image.png'
      link.click()
    },

    resetImage() {
      this.originalImageSrc = null
      this.processedImageSrc = null
      this.originalImage = null
      this.histogramData = null
      this.selectedFileName = ''
      this.imageStatus = null
      this.transformationPoints = [
        { x: 0, y: 0 },
        { x: 255, y: 255 }
      ]

      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = ''
      }

      this.$nextTick(() => {
        this.drawTransformationCurve()
      })
    },

    applyTransformation() {
      if (!this.originalImage) return
      const canvas = document.createElement('canvas')
      canvas.width = this.originalImage.width
      canvas.height = this.originalImage.height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(this.originalImage, 0, 0)

      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      const data = imageData.data

      // Применяем преобразование
      for (let i = 0; i < data.length; i += 4) {
        data[i] = this.getTransformedValue(data[i])
        data[i + 1] = this.getTransformedValue(data[i + 1])
        data[i + 2] = this.getTransformedValue(data[i + 2])
      }

      ctx.putImageData(imageData, 0, 0)
      this.processedImageSrc = canvas.toDataURL()

      // Обновляем гистограмму после преобразования
      const transformedImageData = ctx.getImageData(
        0,
        0,
        canvas.width,
        canvas.height
      )
      const transformedData = transformedImageData.data

      const histogram = new Array(256).fill(0)
      let sum = 0
      let sumSquares = 0
      let min = 255
      let max = 0

      for (let i = 0; i < transformedData.length; i += 4) {
        const r = transformedData[i]
        const g = transformedData[i + 1]
        const b = transformedData[i + 2]
        const gray = Math.round(0.299 * r + 0.587 * g + 0.114 * b)

        histogram[gray]++
        sum += gray
        sumSquares += gray * gray

        if (gray < min) min = gray
        if (gray > max) max = gray
      }

      const pixelCount = transformedData.length / 4
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
      })
    },

    drawHistogram() {
      const canvas = this.$refs.histogramCanvas
      if (!canvas || !this.histogramData) return

      const ctx = canvas.getContext('2d')
      const width = canvas.offsetWidth
      const height = canvas.offsetHeight

      canvas.width = width
      canvas.height = height

      const histogram = this.histogramData.histogram
      const maxValue = Math.max(...histogram)

      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, width, height)

      ctx.strokeStyle = '#0066cc'
      ctx.fillStyle = 'rgba(0, 102, 204, 0.2)'
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

      ctx.lineTo(255 * barWidth, height)
      ctx.lineTo(0, height)
      ctx.closePath()
      ctx.fill()

      // Сетка
      ctx.strokeStyle = '#e2e8f0'
      ctx.lineWidth = 0.5

      for (let i = 0; i <= 10; i++) {
        const x = (i / 10) * width
        ctx.beginPath()
        ctx.moveTo(x, 0)
        ctx.lineTo(x, height)
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
