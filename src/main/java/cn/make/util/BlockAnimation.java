package cn.make.util;

import java.awt.Color;

import chad.phobos.api.events.render.Render3DEvent;
import cn.make.util.skid.RenderUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockAnimation {
	//定义一些变量
	private Color color; //颜色
	private int alpha; //透明度
	private long expandTime; //扩散时间
	private long moveTime; //移动时间
	private long shrinkTime; //收缩时间
	private BlockPos startPos; //开始位置
	private BlockPos endPos; //结束位置
	private AxisAlignedBB startBB; //开始方块
	private AxisAlignedBB currentBB; //当前方块
	private long startTime; //开始时间
	private long endTime; //结束时间
	private boolean started; //是否开始
	private boolean ended; //是否结束

	//构造函数，接收颜色和透明度
	public BlockAnimation(Color color, int alpha, long expTime, long movTime, long shrTime) {
		this.color = color;
		this.alpha = alpha;
		this.expandTime = expTime;
		this.moveTime = movTime;
		this.shrinkTime = shrTime;
		clearAll(); //调用clear方法，重置所有的变量
		reg();
	}
	public void reg() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	public void unreg() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	public void changeColor(Color color, int alpha) {
		this.color = color;
		this.alpha = alpha;
	}
	public void changeTimes(long expTime, long movTime, long shrTime) {
		clearAll();
		this.expandTime = expTime;
		this.moveTime = movTime;
		this.shrinkTime = shrTime;
	}

	//start方法，接收一个BlockPos，从这个BlockPos中心的一个小点快速扩散到整个方块
	public void start(BlockPos pos) {
		if (started) return; //如果已经开始了，就直接返回
		startPos = pos; //记录开始位置
		startBB = new AxisAlignedBB(pos); //创建开始方块
		currentBB = startBB.grow(0.01); //创建当前方块，初始大小为0.01
		startTime = System.currentTimeMillis(); //记录开始时间
		started = true; //标记为已开始
	}

	//change方法，接收一个BlockPos，如果之前没有start则直接返回，否则从start渲染的地方平滑地移动到新的BlockPos上
	public void change(BlockPos pos) {
		if (!started) return; //如果没有开始，就直接返回
		endPos = pos; //记录结束位置
		endTime = System.currentTimeMillis() + moveTime; //计算结束时间，加上移动时间
	}

	//end方法，不需要接受任何变量，如果之前已经开始渲染的话就快速变小地终止渲染
	public void end() {
		if (!started || ended) return; //如果没有开始或者已经结束，就直接返回
		endTime = System.currentTimeMillis() + shrinkTime; //计算结束时间，加上收缩时间
		ended = true; //标记为已结束
	}

	public void clearAll() {
		this.startPos = null;
		this.endPos = null;
		this.startBB = null;
		this.currentBB = null;
		this.startTime = 0;
		this.endTime = 0;
		this.started = false;
		this.ended = false;
	}
	public boolean isStarted() {
		return this.started;
	}
	public boolean isEnded() {
		return this.ended;
	}

	//render方法，在每次游戏渲染3D时被调用，用来渲染当前方块
	@SubscribeEvent
	public void on3D(Render3DEvent event) {
		if (!started) return; //如果没有开始，就直接返回

		long currentTime = System.currentTimeMillis(); //获取当前时间

		if (currentTime >= endTime) { //如果当前时间超过了结束时间，就停止渲染并返回，并且调用clear并取消注册事件监听器
			clearAll();
			unreg();
			return;
		}

		double progress = (double)(currentTime - startTime) / expandTime; //计算扩散进度，从0到1
		if (progress > 1) progress = 1; //如果超过1，就设为1

		double shrinkProgress = 0; //定义收缩进度，从0到1
		if (ended) { //如果已经结束，就计算收缩进度
			shrinkProgress = (double)(currentTime - (endTime - shrinkTime)) / shrinkTime;
			if (shrinkProgress > 1) shrinkProgress = 1;
		}

		double moveProgress = 0; //定义移动进度，从0到1
		if (endPos != null) { //如果有结束位置，就计算移动进度
			moveProgress = (double)(currentTime - startTime - expandTime) / moveTime;
			if (moveProgress > 1) moveProgress = 1;
		}

		currentBB = startBB.grow(0.5 * progress); //根据扩散进度，更新当前方块的大小

		if (endPos != null) { //如果有结束位置，就根据移动进度，更新当前方块的位置
			double x = startPos.getX() + (endPos.getX() - startPos.getX()) * moveProgress;
			double y = startPos.getY() + (endPos.getY() - startPos.getY()) * moveProgress;
			double z = startPos.getZ() + (endPos.getZ() - startPos.getZ()) * moveProgress;
			currentBB = currentBB.offset(x - currentBB.minX, y - currentBB.minY, z - currentBB.minZ);
		}

		if (ended) { //如果已经结束，就根据收缩进度，更新当前方块的大小
			currentBB = currentBB.shrink(0.5 * shrinkProgress);
		}

		RenderUtil.drawBBFill(currentBB, color, alpha); //调用RenderUtil.drawBBFill方法，渲染当前方块
	}
}
