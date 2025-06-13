package ai;

import game.PowerUp;
import game.Target;
import game.TankAIBase;
import game.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TankAI extends TankAIBase {

    public String getPlayerName() {
        return "tuff group";  
    }

    public int getPlayerPeriod() {
        return 5;                
    }

    public boolean updateAI() {
        Vec2 tankPos = getTankPos();           
        PowerUp[] powerUps = getPowerUps();    
        Target[] targets = getTargets();      

        Vec2 myTargetPos = new Vec2(0, 0);

        List<Vec2> targetsInRange = new ArrayList<Vec2>();
        for (int i = 0; i < targets.length; i++) {
            if (targets[i].getPos().distance(tankPos) <= getTankShotRange()) {
                targetsInRange.add(targets[i].getPos());
            }
        }
        for (Vec2 pos : targetsInRange) {
            queueCmd("shoot", pos.subtract(tankPos));
        }
            

        if (getTankVel().x == 0 && getTankVel().y == 0 && getTankAngVel() == 0) {
            List<Vec2> objectives = new ArrayList<>();
            for (PowerUp powerUp : powerUps) {
                objectives.add(powerUp.getPos());
            }

            Path optimalPath = findOptimalPath(tankPos, objectives);
            System.out.println(optimalPath.moves);
            //execute the first move toward the optimal path
            if (optimalPath.moves.get(0).x == tankPos.x || optimalPath.moves.get(0).y == tankPos.y) {
                queueCmd("move", optimalPath.moves.get(0).subtract(tankPos));
            } else {
                queueCmd("move", optimalPath.moves.get(0).subtract(tankPos));
            }
        }
        return true;
    }

    //brute force traveling salesman 
    private Path findOptimalPath(Vec2 tankPos, List<Vec2> objectives) {
        List<Vec2> bestPath = null;
        int minDistance = Integer.MAX_VALUE;

        List<List<Vec2>> permutations = generatePermutations(objectives);

        for (List<Vec2> path : permutations) {
            int distance = calculatePathDistance(tankPos, path);
            if (distance < minDistance) {
                minDistance = distance;
                bestPath = path;
            }
        }

        List<Vec2> moves = generateMovesForPath(tankPos, bestPath);
        return new Path(bestPath, moves, minDistance);
    }

    private int calculatePathDistance(Vec2 start, List<Vec2> path) {
        int distance = 0;
        Vec2 current = start;

        for (Vec2 objective : path) {
            distance += manhattanDistance(current, objective); 
            current = objective;
        }

        return distance;
    }

    private List<Vec2> generateMovesForPath(Vec2 start, List<Vec2> path) {
        List<Vec2> moves = new ArrayList<>();
        Vec2 current = start;
        for (Vec2 objective : path) {
            if (!current.equals(objective)) {
                if (objective.x == current.x || objective.y == current.y) {
                    moves.add(objective);
                } else {
                    if (Math.abs(objective.x-current.x) < Math.abs(objective.y-current.y)) {
                        Vec2 v = new Vec2(objective.x, current.y);
                        moves.add(v);
                        moves.add(objective);
                    } else {
                        Vec2 v = new Vec2(current.x, objective.y);
                        moves.add(v);
                        moves.add(objective);
                    }
                }
            }
            current = objective;
        }

        return moves;
    }

    //make all permutations
    private List<List<Vec2>> generatePermutations(List<Vec2> list) {
        List<List<Vec2>> permutations = new ArrayList<>();
        permute(list, 0, permutations);
        return permutations;
    }

    private void permute(List<Vec2> list, int start, List<List<Vec2>> result) {
        if (start == list.size()) {
            result.add(new ArrayList<>(list));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            Collections.swap(list, i, start);
            permute(list, start + 1, result);
            Collections.swap(list, i, start);
        }
    }

    private double manhattanDistance(Vec2 current, Vec2 other) {
        return Math.abs(current.x-other.x) + Math.abs(current.y - other.y);
    }

    private static class Path {
        List<Vec2> path;    //objectives
        List<Vec2> moves;   //moves
        int totalMoves;     //moves to complete path

        Path(List<Vec2> path, List<Vec2> moves, int totalMoves) {
            this.path = path;
            this.moves = moves;
            this.totalMoves = totalMoves;
        }
    }
}
