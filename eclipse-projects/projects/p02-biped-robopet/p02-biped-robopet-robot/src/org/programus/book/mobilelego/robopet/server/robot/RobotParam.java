package org.programus.book.mobilelego.robopet.server.robot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 机器人数据类，用来存储机器人相关数据
 * @author programus
 *
 */
public class RobotParam {
	public static enum Mood {
		Happy,
		Normal,
		Sad,
		Angry,
		Crazy,
		Tired,
	}
	/** 机器人状态保存文件名 */
	private static final String FILE_NAME = "biped-robopet.status";
	
	/** 每次惹恼机器人时的怒气值增量 */
	private static final int ANGER_LEVEL = 1000 * 20;
	/** 每次让机器人情绪降低时的高兴情绪值减量 */
	private static final int SAD_LEVEL = 1000 * 10;
	/** 情绪向高兴方向发展时，每毫秒的高兴情绪值增量 */
	private static final int HAPPY_STEP = 1;
	/** 情绪向悲伤方向发展时，每毫秒的高兴情绪值增量 */
	private static final int SAD_STEP = -1;
	
	/** 满血，休息需要达到此值，方可活动 */
	private static final int FULL_HP = 1000 * 60 * 10 * 5;
	/** 判断是否生气的怒气高临界值，要超过此值才会生气 */
	private static final int ANGER_HIGH_CRITICAL = ANGER_LEVEL * 3;
	/** 判断是否生气的怒气低临界值，生气时要降到此值以下才会消气 */
	private static final int ANGER_LOW_CRITICAL = 0;
	
	/** 判断是否高兴的高临界值，要超过此值才会高兴 */
	private static final int HAPPY_HIGH_CRITICAL = 1000 * 20;
	/** 判断是否高兴的低临界值，高兴时要降到此值以下才会不高兴 */
	private static final int HAPPY_LOW_CRITICAL = 1000 * 5;
	
	/** 判断是否悲伤的高临界值，悲伤时要超过此值才能恢复平静 */
	private static final int SAD_HIGH_CRITICAL = -1000 * 5;
	/** 判断是否悲伤的低临界值，要低于此值才能变成悲伤 */
	private static final int SAD_LOW_CRITICAL = -1000 * 20;
	
	/** 体力值 */
	private int healthPoint;
	/** 高兴情绪值 */
	private int happyPoint;
	/** 怒气值 */
	private int angerPoint;
	/** 宠物活动总时间 */
	private long liveTime;
	/** 更新上述数字时的系统时间 */
	private long updateTime;
	/** 当前情绪 */
	private Mood mood;
	
	/** 每毫秒消耗体力值 */
	private int healthConsume;
	/** 每毫秒增长的高兴情绪值 */
	private int happyIncrease;
	
	public RobotParam() {
		// 试图从文件加载，失败时使用默认值。
		try {
			this.load();
		} catch (IOException e) {
			this.liveTime = this.happyPoint = this.angerPoint = 0;
			this.healthPoint = FULL_HP;
			this.updateTime = System.currentTimeMillis();
		}
	}
	
	/**
	 * 设置每毫秒体力消耗值
	 * @param value
	 */
	public void setHealthConsume(int value) {
		this.updateStatus();
		this.healthConsume = value;
		if (this.healthConsume > 10) {
			this.healthConsume = 10;
		}
		if (this.healthConsume < 1) {
			this.healthConsume = 1;
		}
	}
	
	/**
	 * 惹恼机器人
	 */
	public void annoy() {
		this.updateStatus();
		int d = this.angerPoint % ANGER_LEVEL;
		this.angerPoint -= d;
		if (d > 0) {
			this.angerPoint += ANGER_LEVEL;
		}
		this.angerPoint += ANGER_LEVEL;
	}
	
	/**
	 * 平复机器人情绪
	 */
	public void calmDown() {
		this.updateStatus();
		this.angerPoint = 0;
		this.happyPoint = 0;
		this.mood = Mood.Normal;
	}
	
	/**
	 * 让机器人悲伤一点
	 */
	public void sadden() {
		this.updateStatus();
		this.happyPoint -= SAD_LEVEL;
		this.happyIncrease = SAD_STEP;
	}
	
	/**
	 * 让机器人情绪向高兴方向发展
	 */
	public void please() {
		this.happyIncrease = HAPPY_STEP;
	}
	
	/**
	 * 计算并更新所有机器人情绪数值
	 */
	public void updateStatus() {
		long st = System.currentTimeMillis();
		int dt = (int)(st - this.updateTime);
		this.angerPoint -= dt;
		if (this.angerPoint < 0) {
			this.angerPoint = 0;
		}
		this.happyPoint += dt * this.happyIncrease;
		this.healthPoint -= dt * this.healthConsume;
		if (this.healthPoint < 0) {
			this.healthPoint = 0;
		}
		this.liveTime += dt;
		this.updateTime = st;
	}
	
	/**
	 * 取得情绪值
	 * @return
	 */
	public Mood getMood() {
		Mood result = null;
		if (this.mood == Mood.Tired ? this.healthPoint >= FULL_HP : this.healthPoint <= 0) {
			result = Mood.Tired;
		} else {
			boolean isHappy = this.mood == Mood.Happy || this.mood == Mood.Crazy;
			boolean isAngry = this.mood == Mood.Angry || this.mood == Mood.Crazy;
			boolean isSad = this.mood == Mood.Sad;
			isHappy = isHappy ? this.happyPoint > HAPPY_LOW_CRITICAL : this.happyPoint > HAPPY_HIGH_CRITICAL;
			isAngry = isAngry ? this.angerPoint > ANGER_LOW_CRITICAL : this.angerPoint > ANGER_HIGH_CRITICAL;
			isSad = isSad ? this.happyPoint < SAD_HIGH_CRITICAL : this.happyPoint < SAD_LOW_CRITICAL;
			if (isHappy && isAngry) {
				result = Mood.Crazy;
			} else if (isHappy) {
				result = Mood.Happy;
			} else if (isAngry) {
				result = Mood.Angry;
			} else if (isSad) {
				result = Mood.Sad;
			} else {
				result = Mood.Normal;
			}
		}
		this.mood = result;
		return result;
	}
	
	/**
	 * 将机器人数据写入文件
	 * @throws IOException
	 */
	public void save() throws IOException {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new FileOutputStream(FILE_NAME));
			this.updateStatus();
			out.writeLong(this.liveTime);
			out.writeInt(this.healthPoint);
			out.writeInt(this.angerPoint);
			out.writeInt(this.happyPoint);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	/**
	 * 从文件读取机器人数据
	 * @throws IOException
	 */
	public void load() throws IOException {
		DataInputStream in = null;
		try {
			in = new DataInputStream(new FileInputStream(FILE_NAME));
			this.liveTime = in.readLong();
			this.healthPoint = in.readInt();
			this.angerPoint = in.readInt();
			this.happyPoint = in.readInt();
			this.updateTime = System.currentTimeMillis();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
}
