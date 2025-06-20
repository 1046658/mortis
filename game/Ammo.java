package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Ammo extends GameObject {
    // Constants...
    public static final int AMMO_SPEED = 8;
    public static final double AMMO_RADIUS = 0.15;
	private static final double AMMO_HEIGHT = 2.75;
	private static final double AMMO_STROKE_WIDTH = 0.05;
	private static final Color AMMO_COLOR_FILL_0 = Tank.TANK_COLOR_BODY_FILL_1;
	private static final Color AMMO_COLOR_FILL_1 = Tank.TANK_COLOR_BODY_FILL_2;
	private static final Color AMMO_COLOR_STROKE = Color.BLACK;

    // Member variables...
    private int playerIdx = -1;
    private Vec2 startPos = null;
    private double maxRange = -1.0;
    private Vec2 vel = null;
    private double radius = -1.0;
	private double moveElapsedTime = 0.0;
	// Accessors...
	public int getPlayerIdx() {
		return this.playerIdx;
	}
	public Vec2 getDir() {
		return vel.unit();
	}
	public Vec2 getVel() {
		return Vec2.copy(this.vel);
	}
	public double getRadius() {
		return this.radius;
	}

    // Member functions (methods)...
    protected Ammo(int playerIdx, Vec2 pos, Vec2 dir, double maxRange) {
		// Parent...
		super();

		// Defaults...
		this.playerIdx = playerIdx;
		this.startPos = pos;
        this.pos = pos;
		this.maxRange = maxRange;
		this.vel = Vec2.multiply(dir.unit(), AMMO_SPEED);
		this.radius = AMMO_RADIUS;
		this.timeTillDeath = 0;
    }

	protected void destroy() {
		// Super...
		super.destroy();
	}
	
	protected boolean shouldBeCulled() {
		// Check if on field...
		if (!Util.isInsideField(this.pos)) {
		 	return true;
		}
		
		// Check death timer...
		if (this.timeTillDeath >= 1) {
			return true;
		}
		
		return false;
	}
	
	protected void update(double deltaTime) {
		// Super...
		super.update(deltaTime);

		// If dying, continue...
		if (this.timeTillDeath > 0) {
			// Update it...
			this.timeTillDeath = Math.min(this.timeTillDeath + deltaTime * 10.0, 1.0);
			
			// Scale it down...
			this.radius = AMMO_RADIUS * Math.max(1.0 - this.timeTillDeath, 0.001);
		}
		else {
			Vec2 prevPos = this.pos;
			
			double traveled = Vec2.subtract(this.pos, this.startPos).length();
			double t = Math.min(traveled / this.maxRange, 1.0); // 0 to 1
			double speedFactor = 0.9 - t * t; //Change 0.9 based on how much you want to decelerate
			double currentSpeed = AMMO_SPEED * speedFactor;

			Vec2 step = Vec2.multiply(this.vel.unit(), currentSpeed * deltaTime);
			this.pos = Vec2.add(this.pos, step);
			Vec2 motionVec = Vec2.subtract(this.pos, prevPos);

			// Check for hitting things (do swept collision to avoid missing it)...
			ArrayList<GameObject> gameObjects = Simulation.getGameObjects();
			for (int i = 0; i < gameObjects.size(); ++i) {
				GameObject gameObject = gameObjects.get(i);

				// Targets...
				if (gameObject instanceof Target) {
					Target target = (Target)gameObject;
					if (Util.intersectCircleCapsule(target.pos, target.getRadius(), prevPos, motionVec, this.radius)) {
						// Tell the target about it...

						target.onHitByAmmo(this.playerIdx);
					
						// That's it for us...
						this.timeTillDeath = Math.max(this.timeTillDeath, 0.0001);
					} else if (currentSpeed <= 0.75) {
						this.timeTillDeath = Math.max(this.timeTillDeath, 0.0001);
					}
				}
				
				// Tanks...
				if (gameObject instanceof Tank) {
					Tank tank = (Tank)gameObject;
					if ((this.playerIdx != tank.getPlayerIdx()) && // Don't let someone hit themselves
					    (Util.intersectCircleCapsule(tank.pos, Tank.BODY_HALFSIZE.x, prevPos, motionVec, this.radius))) {
						// Award points...
						Game.get().awardPoints(Game.POINTS_HIT_OTHER, this.playerIdx);
						Util.log("(hit tank: score +" + Game.POINTS_HIT_OTHER + ")");
						
						// Kick the tank back (if you can)...
						tank.kickBackTank(Vec2.subtract(tank.pos, this.pos).unit());
						
						// That's it for us...
						this.timeTillDeath = Math.max(this.timeTillDeath, 0.0001);
					}
				}
			}
			// Check max range...

			if ((Vec2.subtract(this.pos, this.startPos)).lengthSqr() >= (this.maxRange * this.maxRange)) {
				// That's it for us...
				this.timeTillDeath = Math.max(this.timeTillDeath, 0.0001);
			}
		}
	}

    protected double calcDrawScale() {
		return Math.max(1.0 - this.timeTillDeath, 0.001) * Math.min(timeSinceBorn * 5, 1);
	}

	protected double calcDrawHeight(double height, double scale) {
		return height * Math.min(timeSinceBorn * 5, 1) * scale * (1 - (Vec2.subtract(this.pos, this.startPos)).length() / this.maxRange);
	}

	protected void drawShadow(Graphics2D g) {
		// Setup...
		double scale = calcDrawScale();
		Color colorShadow = Util.colorLerp(World.COLOR_BACKGROUND, World.COLOR_SHADOW, timeSinceBorn * 2.0f);

		// Body...
		Draw.drawRectShadow(g, this.pos, calcDrawHeight(AMMO_HEIGHT, scale), new Vec2(AMMO_RADIUS, AMMO_RADIUS), scale, colorShadow, AMMO_RADIUS);
    }

    protected void draw(Graphics2D g) {
		// Setup...
		double scale = calcDrawScale();
		double height = calcDrawHeight(AMMO_HEIGHT, scale);
		Color ammoColor = (this.playerIdx == 0) ? AMMO_COLOR_FILL_0 : AMMO_COLOR_FILL_1;
		Color colorFill = Util.colorLerp(World.COLOR_BACKGROUND, ammoColor, timeSinceBorn * 5.0f);
		Color colorStroke = Util.colorLerp(World.COLOR_BACKGROUND, AMMO_COLOR_STROKE, timeSinceBorn * 5.0f);

		// Body...
		Draw.drawRect(g, this.pos, height, new Vec2(AMMO_RADIUS, AMMO_RADIUS), scale, colorFill, colorStroke, AMMO_STROKE_WIDTH, AMMO_RADIUS * 2);
    }
}
