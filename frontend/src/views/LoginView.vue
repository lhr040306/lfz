<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import Matter from "matter-js";
import { login, register } from "@/api/modules/auth";
import { useAuthStore } from "@/stores/auth";

type AuthTab = "login" | "register";
type CanvasCtx = CanvasRenderingContext2D;

const router = useRouter();
const authStore = useAuthStore();

const activeTab = ref<AuthTab>("login");
const loading = ref(false);
const rememberMe = ref(true);
const sceneCanvas = ref<HTMLCanvasElement>();

const loginForm = reactive({
  username: "",
  password: ""
});

const registerForm = reactive({
  username: "",
  password: "",
  realName: "",
  phone: ""
});

let engine: Matter.Engine | null = null;
let chassis: Matter.Body | null = null;
let wheelFront: Matter.Body | null = null;
let wheelRear: Matter.Body | null = null;
const roadSegments: Matter.Body[] = [];

const segmentWidth = 1100;
const segmentHeight = 54;
const wheelRadius = 14;
const normalSpeed = 3.1;
const boostSpeed = 6.3;
const carStartX = 220;
const wheelOffsetX = 34;
const chassisYOffset = 52;
const wheelYOffset = 30;

let animationId = 0;
let roadY = 0;
let viewportWidth = 0;
let viewportHeight = 0;
let boostUntil = 0;
let carX = carStartX;
let wheelSpin = 0;

function triggerBoost() {
  boostUntil = performance.now() + 1200;
}

async function onLogin() {
  triggerBoost();
  loading.value = true;
  try {
    const data = await login(loginForm);
    authStore.setAuth({
      token: data.token,
      username: data.username,
      roleCode: data.roleCode
    });
    ElMessage.success({
      message: "登录成功",
      duration: 800
    });
    router.push("/cars");
  } finally {
    loading.value = false;
  }
}

async function onRegister() {
  loading.value = true;
  try {
    await register(registerForm);
    ElMessage.success("注册成功，请登录");
    activeTab.value = "login";
    loginForm.username = registerForm.username;
    loginForm.password = "";
  } finally {
    loading.value = false;
  }
}

function pseudo(seed: number, min: number, max: number) {
  const value = (Math.sin(seed * 15.238) + 1) / 2;
  return min + value * (max - min);
}

function drawRoundRect(ctx: CanvasCtx, x: number, y: number, w: number, h: number, r: number) {
  const radius = Math.min(r, w / 2, h / 2);
  ctx.beginPath();
  ctx.moveTo(x + radius, y);
  ctx.arcTo(x + w, y, x + w, y + h, radius);
  ctx.arcTo(x + w, y + h, x, y + h, radius);
  ctx.arcTo(x, y + h, x, y, radius);
  ctx.arcTo(x, y, x + w, y, radius);
  ctx.closePath();
}

function drawWheel(ctx: CanvasCtx, x: number, y: number, angle: number) {
  ctx.save();
  ctx.translate(x, y);
  ctx.rotate(angle);

  const tire = ctx.createRadialGradient(-2, -2, 2, 0, 0, wheelRadius + 2);
  tire.addColorStop(0, "#1f2937");
  tire.addColorStop(1, "#0a0f18");
  ctx.fillStyle = tire;
  ctx.beginPath();
  ctx.arc(0, 0, wheelRadius + 2, 0, Math.PI * 2);
  ctx.fill();

  ctx.strokeStyle = "rgba(255, 255, 255, 0.2)";
  ctx.lineWidth = 1.4;
  ctx.beginPath();
  ctx.arc(0, 0, wheelRadius - 1.2, 0, Math.PI * 2);
  ctx.stroke();

  const rim = ctx.createRadialGradient(-1, -1, 1, 0, 0, 10);
  rim.addColorStop(0, "#4a4a4a");
  rim.addColorStop(1, "#111111");
  ctx.fillStyle = rim;
  ctx.beginPath();
  ctx.arc(0, 0, 9.6, 0, Math.PI * 2);
  ctx.fill();

  ctx.fillStyle = "#222222";
  for (let i = 0; i < 8; i += 1) {
    const a = ((Math.PI * 2) / 8) * i;
    ctx.beginPath();
    ctx.moveTo(Math.cos(a) * 2, Math.sin(a) * 2);
    ctx.lineTo(Math.cos(a + 0.12) * 9, Math.sin(a + 0.12) * 9);
    ctx.lineTo(Math.cos(a - 0.12) * 9, Math.sin(a - 0.12) * 9);
    ctx.closePath();
    ctx.fill();
  }

  ctx.fillStyle = "#ff1a1a";
  ctx.beginPath();
  ctx.arc(0, 0, 2, 0, Math.PI * 2);
  ctx.fill();

  ctx.restore();
}

function drawSky(ctx: CanvasCtx) {
  const sky = ctx.createLinearGradient(0, 0, 0, viewportHeight);
  sky.addColorStop(0, "#73baff");
  sky.addColorStop(0.6, "#bde0ff");
  sky.addColorStop(1, "#e6f3ff");
  ctx.fillStyle = sky;
  ctx.fillRect(0, 0, viewportWidth, viewportHeight);

  ctx.save();
  ctx.shadowBlur = 40;
  ctx.shadowColor = "#ffeb3b";
  ctx.fillStyle = "#ffe066";
  ctx.beginPath();
  ctx.arc(viewportWidth - 150, 120, 36, 0, Math.PI * 2);
  ctx.fill();
  ctx.restore();
}

function drawBuildings(ctx: CanvasCtx, cameraX: number) {
  const parallax = cameraX * 0.45;
  const start = Math.floor((parallax - 320) / 230);
  const end = Math.floor((parallax + viewportWidth + 320) / 230);

  for (let i = start; i <= end; i += 1) {
    const x = i * 230 - parallax;
    const width = pseudo(i + 2, 52, 92);
    const height = pseudo(i + 19, 90, 188);
    const y = roadY - segmentHeight / 2 - height;
    ctx.fillStyle = i % 2 === 0 ? "#9fb4ca" : "#8ea5be";
    ctx.fillRect(x, y, width, height);

    ctx.fillStyle = "rgba(255, 255, 255, 0.52)";
    const rows = Math.max(2, Math.floor(height / 28));
    const cols = Math.max(2, Math.floor(width / 17));
    for (let r = 0; r < rows; r += 1) {
      for (let c = 0; c < cols; c += 1) {
        ctx.fillRect(x + 7 + c * 14, y + 8 + r * 20, 5, 8);
      }
    }
  }
}

function drawTrees(ctx: CanvasCtx, cameraX: number) {
  const parallax = cameraX * 0.82;
  const start = Math.floor((parallax - 260) / 170);
  const end = Math.floor((parallax + viewportWidth + 260) / 170);

  for (let i = start; i <= end; i += 1) {
    const x = i * 170 - parallax;
    const trunkTop = roadY - segmentHeight / 2 - 48;
    const canopyH = pseudo(i + 37, 24, 40);
    const canopyW = pseudo(i + 51, 30, 48);

    ctx.fillStyle = "#7d5b3d";
    ctx.fillRect(x - 4, trunkTop, 8, 48);

    ctx.fillStyle = i % 2 === 0 ? "#3f8f51" : "#2f7e42";
    drawRoundRect(ctx, x - canopyW / 2, trunkTop - canopyH + 8, canopyW, canopyH, 12);
    ctx.fill();
  }
}

function drawRoad(ctx: CanvasCtx, cameraX: number, speed: number) {
  for (const segment of roadSegments) {
    const left = segment.position.x - segmentWidth / 2 - cameraX;
    const top = roadY - segmentHeight / 2;

    ctx.fillStyle = "#374151";
    ctx.fillRect(left, top, segmentWidth, segmentHeight);
    ctx.fillStyle = "#212936";
    ctx.fillRect(left, top + segmentHeight - 10, segmentWidth, 10);
  }

  ctx.strokeStyle = "rgba(255, 255, 255, 0.75)";
  ctx.lineWidth = 3;
  ctx.setLineDash([24, 20]);
  ctx.lineDashOffset = -performance.now() * speed * 0.06;
  ctx.beginPath();
  ctx.moveTo(0, roadY - 4);
  ctx.lineTo(viewportWidth, roadY - 4);
  ctx.stroke();
  ctx.setLineDash([]);
}

function drawCar(ctx: CanvasCtx, cameraX: number) {
  if (!chassis || !wheelFront || !wheelRear) {
    return;
  }

  const carX = chassis.position.x - cameraX;
  const carY = chassis.position.y;

  // 地面阴影，增加压地感
  ctx.fillStyle = "rgba(7, 18, 37, 0.26)";
  ctx.beginPath();
  ctx.ellipse(carX, carY + 19, 86, 8, 0, 0, Math.PI * 2);
  ctx.fill();

  drawWheel(ctx, wheelRear.position.x - cameraX, wheelRear.position.y, wheelRear.angle);
  drawWheel(ctx, wheelFront.position.x - cameraX, wheelFront.position.y, wheelFront.angle);

  ctx.save();
  ctx.translate(carX, carY);
  ctx.rotate(chassis.angle);

  // 主车身（烈焰红）
  const body = ctx.createLinearGradient(-62, -18, 64, 14);
  body.addColorStop(0, "#990000");
  body.addColorStop(0.5, "#ff1a1a");
  body.addColorStop(1, "#cc0000");
  ctx.fillStyle = body;
  ctx.beginPath();
  ctx.moveTo(-62, 7);
  ctx.quadraticCurveTo(-60, -7, -45, -14);
  ctx.lineTo(-20, -16);
  ctx.quadraticCurveTo(2, -28, 24, -26);
  ctx.lineTo(44, -22);
  ctx.quadraticCurveTo(59, -18, 66, -8);
  ctx.lineTo(66, 4);
  ctx.quadraticCurveTo(63, 10, 50, 12);
  ctx.lineTo(-39, 12);
  ctx.quadraticCurveTo(-58, 12, -62, 7);
  ctx.closePath();
  ctx.fill();

  // 车门下方侧裙（碳黑）
  const skirt = ctx.createLinearGradient(-30, 6, 52, 10);
  skirt.addColorStop(0, "#111111");
  skirt.addColorStop(1, "#2a2a2a");
  ctx.fillStyle = skirt;
  drawRoundRect(ctx, -31, 7, 84, 5, 3);
  ctx.fill();

  // 高光线条（中轴展开）
  const gloss = ctx.createLinearGradient(-44, -15, 44, -8);
  gloss.addColorStop(0, "rgba(255, 255, 255, 0.12)");
  gloss.addColorStop(0.5, "rgba(255, 255, 255, 0.36)");
  gloss.addColorStop(1, "rgba(255, 255, 255, 0.12)");
  ctx.fillStyle = gloss;
  ctx.beginPath();
  ctx.moveTo(-40, -10);
  ctx.quadraticCurveTo(0, -20, 40, -10);
  ctx.lineTo(36, -7);
  ctx.quadraticCurveTo(0, -13, -36, -7);
  ctx.closePath();
  ctx.fill();

  // 座舱（近中轴）
  const cabin = ctx.createLinearGradient(-34, -36, 34, -16);
  cabin.addColorStop(0, "#78b4e1");
  cabin.addColorStop(1, "#3f84bf");
  ctx.fillStyle = cabin;
  ctx.beginPath();
  ctx.moveTo(-31, -17);
  ctx.quadraticCurveTo(-18, -36, 0, -36);
  ctx.quadraticCurveTo(18, -36, 31, -17);
  ctx.closePath();
  ctx.fill();

  // 车窗（左右对称）
  const windowGrad = ctx.createLinearGradient(-24, -31, 24, -19);
  windowGrad.addColorStop(0, "rgba(225, 245, 255, 0.95)");
  windowGrad.addColorStop(1, "rgba(165, 207, 240, 0.95)");
  ctx.fillStyle = windowGrad;
  ctx.beginPath();
  ctx.moveTo(-22, -29);
  ctx.lineTo(-4, -29);
  ctx.lineTo(-2, -20);
  ctx.lineTo(-24, -20);
  ctx.closePath();
  ctx.fill();
  ctx.beginPath();
  ctx.moveTo(4, -29);
  ctx.lineTo(22, -29);
  ctx.lineTo(24, -20);
  ctx.lineTo(2, -20);
  ctx.closePath();
  ctx.fill();

  // 中柱和门线
  ctx.strokeStyle = "rgba(12, 37, 67, 0.45)";
  ctx.lineWidth = 1.2;
  ctx.beginPath();
  ctx.moveTo(0, -29);
  ctx.lineTo(0, -20);
  ctx.stroke();
  ctx.beginPath();
  ctx.moveTo(0, -16);
  ctx.lineTo(0, 9);
  ctx.stroke();

  // 前后灯（尺寸对称，颜色区分方向）
  const headLamp = ctx.createLinearGradient(56, -9, 65, -3);
  headLamp.addColorStop(0, "#ffe39d");
  headLamp.addColorStop(1, "#ffc53f");
  ctx.fillStyle = headLamp;
  drawRoundRect(ctx, 58, -8, 7, 6, 2);
  ctx.fill();

  const tailLamp = ctx.createLinearGradient(-65, -9, -56, -3);
  tailLamp.addColorStop(0, "#ff6f7f");
  tailLamp.addColorStop(1, "#ff405f");
  ctx.fillStyle = tailLamp;
  drawRoundRect(ctx, -65, -8, 7, 6, 2);
  ctx.fill();

  // 前进气口（右侧稍强调）
  ctx.fillStyle = "rgba(6, 22, 40, 0.88)";
  drawRoundRect(ctx, 50, 4, 11, 4, 2);
  ctx.fill();

  // 尾部排气（左侧）
  ctx.fillStyle = "#7a8ba1";
  drawRoundRect(ctx, -66, 4, 5, 3, 1);
  ctx.fill();

  // 狂暴橙排气火焰
  const time = performance.now();
  const flameFlicker = Math.random() * 0.4 + 0.6;
  const flameLength = 15 + Math.sin(time * 0.05) * 8 + Math.random() * 12;
  ctx.save();
  ctx.translate(-65, 5.5);
  ctx.beginPath();
  ctx.moveTo(0, -2);
  ctx.lineTo(-flameLength, 0);
  ctx.lineTo(0, 2);
  ctx.closePath();
  const flameGrad = ctx.createLinearGradient(0, 0, -flameLength, 0);
  flameGrad.addColorStop(0, `rgba(255, 255, 255, ${flameFlicker})`);
  flameGrad.addColorStop(0.3, `rgba(255, 140, 0, ${flameFlicker})`);
  flameGrad.addColorStop(1, "rgba(255, 0, 0, 0)");
  ctx.fillStyle = flameGrad;
  ctx.fill();
  ctx.restore();

  // 外轮廓描边
  ctx.strokeStyle = "rgba(5, 20, 35, 0.4)";
  ctx.lineWidth = 1;
  ctx.beginPath();
  ctx.moveTo(-62, 7);
  ctx.quadraticCurveTo(-60, -7, -45, -14);
  ctx.lineTo(-20, -16);
  ctx.quadraticCurveTo(2, -28, 24, -26);
  ctx.lineTo(44, -22);
  ctx.quadraticCurveTo(59, -18, 66, -8);
  ctx.lineTo(66, 4);
  ctx.quadraticCurveTo(63, 10, 50, 12);
  ctx.lineTo(-39, 12);
  ctx.quadraticCurveTo(-58, 12, -62, 7);
  ctx.closePath();
  ctx.stroke();

  ctx.restore();
}

function drawScene(ctx: CanvasCtx, speed: number) {
  if (!chassis) {
    return;
  }

  const cameraX = chassis.position.x - viewportWidth * 0.35;
  drawSky(ctx);
  drawBuildings(ctx, cameraX);
  drawTrees(ctx, cameraX);
  drawRoad(ctx, cameraX, speed);
  drawCar(ctx, cameraX);
}

function recycleRoad(cameraX: number) {
  if (roadSegments.length === 0) {
    return;
  }

  let farthestX = Math.max(...roadSegments.map((segment) => segment.position.x));
  for (const segment of roadSegments) {
    if (segment.position.x + segmentWidth / 2 < cameraX - 220) {
      farthestX += segmentWidth;
      Matter.Body.setPosition(segment, { x: farthestX, y: roadY });
    }
  }
}

function resizeScene() {
  const canvas = sceneCanvas.value;
  if (!canvas) {
    return;
  }

  const nextWidth = window.innerWidth;
  const nextHeight = Math.max(window.innerHeight, 680);
  if (nextWidth === viewportWidth && nextHeight === viewportHeight) {
    return;
  }

  const oldRoadY = roadY;
  viewportWidth = nextWidth;
  viewportHeight = nextHeight;
  canvas.width = viewportWidth;
  canvas.height = viewportHeight;
  roadY = viewportHeight - 120;

  if (oldRoadY > 0) {
    for (const segment of roadSegments) {
      Matter.Body.setPosition(segment, { x: segment.position.x, y: roadY });
    }
  }

  if (chassis && wheelFront && wheelRear) {
    Matter.Body.setPosition(chassis, { x: carX, y: roadY - chassisYOffset });
    Matter.Body.setPosition(wheelFront, { x: carX + wheelOffsetX, y: roadY - wheelYOffset });
    Matter.Body.setPosition(wheelRear, { x: carX - wheelOffsetX, y: roadY - wheelYOffset });
  }
}

function setupScene() {
  const canvas = sceneCanvas.value;
  if (!canvas) {
    return;
  }

  const context = canvas.getContext("2d");
  if (!context) {
    return;
  }

  engine = Matter.Engine.create({
    gravity: { x: 0, y: 0 }
  });

  resizeScene();

  roadSegments.length = 0;
  roadSegments.push(
    Matter.Bodies.rectangle(segmentWidth / 2, roadY, segmentWidth, segmentHeight, { isStatic: true, friction: 1 }),
    Matter.Bodies.rectangle(segmentWidth * 1.5, roadY, segmentWidth, segmentHeight, { isStatic: true, friction: 1 }),
    Matter.Bodies.rectangle(segmentWidth * 2.5, roadY, segmentWidth, segmentHeight, { isStatic: true, friction: 1 })
  );

  carX = carStartX;
  wheelSpin = 0;

  chassis = Matter.Bodies.rectangle(carX, roadY - chassisYOffset, 104, 24, {
    friction: 0.8,
    density: 0.002,
    chamfer: { radius: 8 }
  });
  wheelRear = Matter.Bodies.circle(carX - wheelOffsetX, roadY - wheelYOffset, wheelRadius, {
    friction: 1.1,
    restitution: 0.04,
    density: 0.0012
  });
  wheelFront = Matter.Bodies.circle(carX + wheelOffsetX, roadY - wheelYOffset, wheelRadius, {
    friction: 1.1,
    restitution: 0.04,
    density: 0.0012
  });

  Matter.World.add(engine.world, [...roadSegments, chassis, wheelRear, wheelFront]);

  const frame = () => {
    if (!chassis || !wheelFront || !wheelRear) {
      return;
    }

    const speed = performance.now() < boostUntil ? boostSpeed : normalSpeed;
    carX += speed;
    wheelSpin += speed / wheelRadius;

    Matter.Body.setPosition(chassis, { x: carX, y: roadY - chassisYOffset });
    Matter.Body.setAngle(chassis, 0);

    Matter.Body.setPosition(wheelFront, { x: carX + wheelOffsetX, y: roadY - wheelYOffset });
    Matter.Body.setPosition(wheelRear, { x: carX - wheelOffsetX, y: roadY - wheelYOffset });
    Matter.Body.setAngle(wheelFront, wheelSpin);
    Matter.Body.setAngle(wheelRear, wheelSpin);

    recycleRoad(carX - viewportWidth * 0.35);
    drawScene(context, speed);

    animationId = window.requestAnimationFrame(frame);
  };

  animationId = window.requestAnimationFrame(frame);
}

onMounted(() => {
  setupScene();
  window.addEventListener("resize", resizeScene);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeScene);
  if (animationId) {
    window.cancelAnimationFrame(animationId);
  }

  if (engine) {
    Matter.World.clear(engine.world, false);
    Matter.Engine.clear(engine);
  }

  engine = null;
  chassis = null;
  wheelFront = null;
  wheelRear = null;
  roadSegments.length = 0;
});
</script>

<template>
  <section class="login-screen">
    <canvas ref="sceneCanvas" class="road-canvas"></canvas>
    <div class="scene-mask"></div>

    <div class="panel auth-card">
      <h1>登录到您的账户</h1>
      <p class="subtitle">汽车租赁管理系统 · 毕设演示版</p>
      <el-tabs v-model="activeTab" class="auth-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-width="70px">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button class="login-btn" type="primary" :loading="loading" @click="onLogin">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-width="70px">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="4-20 位字符" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" show-password placeholder="6-20 位字符" />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机">
              <el-input v-model="registerForm.phone" placeholder="可选，11位手机号" />
            </el-form-item>
            <el-form-item>
              <el-button class="login-btn" type="primary" :loading="loading" @click="onRegister">立即注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </section>
</template>

<style scoped>
.login-screen {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: grid;
  place-items: center;
  padding: 24px;
}

.road-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.scene-mask {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background:
    linear-gradient(180deg, rgba(13, 26, 45, 0.24) 0%, rgba(240, 248, 255, 0.12) 42%, rgba(9, 18, 29, 0.2) 100%),
    radial-gradient(circle at 50% 15%, rgba(255, 255, 255, 0.38) 0, transparent 56%);
}

.auth-card {
  position: relative;
  z-index: 2;
  width: min(430px, calc(100% - 20px));
  border-radius: 22px;
  border-color: rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  box-shadow: 0 24px 52px rgba(9, 17, 30, 0.2);
}

.auth-card h1 {
  margin: 0 0 8px;
  font-size: 29px;
  color: #0f2742;
  text-align: center;
}

.subtitle {
  margin: 0 0 6px;
  color: #4d5f77;
  font-size: 14px;
  text-align: center;
}

.login-btn {
  width: 100%;
  height: 42px;
  border-radius: 10px;
  font-weight: 700;
}

:deep(.el-form-item__label) {
  color: #223a58;
  font-weight: 600;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #d5e2f3 inset;
  border-radius: 10px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #9cb5d7 inset;
}

:deep(.auth-tabs .el-tabs__nav) {
  width: 100%;
  display: flex;
}

:deep(.auth-tabs .el-tabs__item) {
  flex: 1;
  padding: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

@media (max-width: 768px) {
  .login-screen {
    padding: 16px;
  }

  .auth-card {
    border-radius: 18px;
    padding: 18px;
  }

  .auth-card h1 {
    font-size: 24px;
  }
}
</style>
