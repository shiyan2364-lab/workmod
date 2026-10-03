#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
# Workmod 整包覆盖全推脚本（Termux）
# 用法：
#   1. 把 workmod-整包 目录传到手机（如 /storage/emulated/0/临时中转站/workmod-整包）
#   2. bash <整包路径>/push_whole.sh
# 作用：整仓替换为完整可编译项目（保留 .git），强制覆盖 master。
#   源码：src/main（服务端/通用）+ src/client（客户端，splitEnvironmentSourceSets）
# ============================================================
set -e

REPO="${REPO:-/storage/emulated/0/临时中转站/ds/workmod}"
WHOLE="${1:-$(dirname "$0")}"

echo "== 目标仓库: $REPO"
echo "== 整包来源: $WHOLE"
[ -f "$WHOLE/build.gradle" ] || { echo "❌ 来源不是整包（缺 build.gradle）"; exit 1; }
[ -d "$WHOLE/src/client/java" ] || { echo "❌ 缺 src/client（客户端源码集）"; exit 1; }

# 若目标已是 git 仓库：先重置到远程，再整仓替换（保留 .git）
if [ -d "$REPO/.git" ]; then
  echo "== 重置到远程当前状态..."
  cd "$REPO"
  git fetch origin 2>/dev/null || true
  git reset --hard origin/master 2>/dev/null || git reset --hard origin/main 2>/dev/null || true
  cd -
fi

mkdir -p "$REPO"
cd "$REPO"
git rev-parse --git-dir >/dev/null 2>&1 || { echo "❌ $REPO 不是 git 仓库，请先 git init 并设置远程 origin"; exit 1; }

echo "== 清空仓库内容（保留 .git）..."
find . -mindepth 1 -maxdepth 1 ! -name '.git' ! -name '.' -exec rm -rf {} +

echo "== 复制整包..."
cp -r "$WHOLE/.github" "$WHOLE/gradle" "$WHOLE/src" "$REPO/" 2>/dev/null || true
cp "$WHOLE/build.gradle" "$WHOLE/gradle.properties" "$WHOLE/settings.gradle" \
   "$WHOLE/.gitignore" "$WHOLE/LICENSE" "$WHOLE/README.md" "$REPO/" 2>/dev/null || true
[ -f "$REPO/build.gradle" ] || { echo "❌ 复制不完整"; exit 1; }

git add -A
git commit -m "整包覆盖: 全部系统源码（道路/断路/守卫/裂隙/峡谷高山/诅咒/成就/交易/饰品栏/小地图/云层，客户端类入 src/client）" || true

echo "== 强制推送 master..."
git push --force-with-lease origin master 2>/dev/null || git push --force origin master

echo ""
echo "✅ 推送完成。查看编译： https://github.com/shiyan2364-lab/workmod/actions"
