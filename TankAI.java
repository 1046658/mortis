package ai;

import game.PowerUp;
import game.Target;
import game.TankAIBase;
import game.Vec2;

public class TankAI extends TankAIBase {
    public String getPlayerName() {
        return "TaAMANGANGOANGOANGOANGOANGABGAr";
    }

    public int getPlayerPeriod() {
        return 3;
    }

    public boolean updateAI() {
        Vec2 tankPos = getTankPos();
        Vec2 tankDir = getTankDir();
        PowerUp[] powerUps = getPowerUps();
        Target[] targets = getTargets();
        
        // Always check for shootable targets first, regardless of current goal
        Target shootableTarget = findShootableTarget(tankPos, tankDir, targets);
        if (shootableTarget != null) {
            Vec2 toTarget = Vec2.subtract(shootableTarget.getPos(), tankPos).unit();
            if (!isAligned(tankDir, toTarget)) {
                queueCmd("turn", toTarget);
                return true;
            }
            queueCmd("shoot", toTarget);
            return true;
        }

        // Continue with existing powerup collection logic
        PowerUp pointsPowerUp = findNearestPowerUpOfType(powerUps, "P", tankPos);
        if (pointsPowerUp != null && Vec2.distance(tankPos, pointsPowerUp.getPos()) < 5.0) {
            return moveTowardsPowerUp(tankPos, tankDir, pointsPowerUp);
        }

        PowerUp rangePowerUp = findNearestPowerUpOfType(powerUps, "R", tankPos);
        if (rangePowerUp != null) {
            return moveTowardsPowerUp(tankPos, tankDir, rangePowerUp);
        }

        // Strategy 4: If other tank is nearby and in range, engage
        if (getOther() != null && Vec2.distance(tankPos, getOther().getPos()) < getTankShotRange()) {
            Vec2 toEnemy = Vec2.subtract(getOther().getPos(), tankPos).unit();
            if (isAligned(tankDir, toEnemy)) {
                queueCmd("shoot", toEnemy);
                return true;
            }
            queueCmd("turn", toEnemy);
            return true;
        }

        // Strategy 5: Move towards nearest target if nothing else to do
        Target nearestTarget = findNearestTarget(targets, tankPos);
        if (nearestTarget != null) {
            return moveTowardsTarget(tankPos, tankDir, nearestTarget);
        }

        // Default behavior: patrol
        queueCmd("move", new Vec2(1, 0));
        return true;
    }
    private Target findShootableTarget(Vec2 tankPos, Vec2 tankDir, Target[] targets) {
        for (Target target : targets) {
            Vec2 toTarget = Vec2.subtract(target.getPos(), tankPos);
            double distance = Vec2.length(toTarget);
            if (distance <= getTankShotRange() && isAligned(tankDir, toTarget.unit())) {
                return target;
            }
        }
        return null;
    }

    private PowerUp findNearestPowerUpOfType(PowerUp[] powerUps, String type, Vec2 pos) {
        PowerUp nearest = null;
        double minDist = Double.MAX_VALUE;
        
        for (PowerUp powerUp : powerUps) {
            if (powerUp.getType().equals(type)) {
                double dist = Vec2.distance(pos, powerUp.getPos());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = powerUp;
                }
            }
        }
        return nearest;
    }

    private Target findNearestTarget(Target[] targets, Vec2 pos) {
        Target nearest = null;
        double minDist = Double.MAX_VALUE;
        
        for (Target target : targets) {
            double dist = Vec2.distance(pos, target.getPos());
            if (dist < minDist) {
                minDist = dist;
                nearest = target;
            }
        }
        return nearest;
    }

    private boolean moveTowardsPowerUp(Vec2 tankPos, Vec2 tankDir, PowerUp powerUp) {
        Vec2 toPowerUp = Vec2.subtract(powerUp.getPos(), tankPos);
        
        // Move full distance horizontally or vertically based on which distance is greater
        Vec2 moveVec;
        if (Math.abs(toPowerUp.x) > Math.abs(toPowerUp.y)) {
            moveVec = new Vec2(toPowerUp.x, 0);
        } else {
            moveVec = new Vec2(0, toPowerUp.y);
        }

        if (!isAligned(tankDir, moveVec.unit())) {
            queueCmd("turn", moveVec.unit());
            return true;
        }
        
        queueCmd("move", moveVec);
        return true;
    }

    private boolean moveTowardsTarget(Vec2 tankPos, Vec2 tankDir, Target target) {
        Vec2 toTarget = Vec2.subtract(target.getPos(), tankPos);
        
        Vec2 moveVec;
        if (Math.abs(toTarget.x) > Math.abs(toTarget.y)) {
            moveVec = new Vec2(toTarget.x, 0);
        } else {
            moveVec = new Vec2(0, toTarget.y);
        }

        if (!isAligned(tankDir, moveVec.unit())) {
            queueCmd("turn", moveVec.unit());
            return true;
        }
        
        queueCmd("move", moveVec);
        return true;
    }

    private boolean isAligned(Vec2 dir1, Vec2 dir2) {
        return Vec2.dot(dir1.unit(), dir2.unit()) > 0.95;
    }
}
