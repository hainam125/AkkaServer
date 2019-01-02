package games;

import java.util.ArrayList;
import java.util.List;

public class Transform {
    public Vector3 position = Vector3.zero;
    public Quaternion rotation = Quaternion.zero;
    public Vector3 bound = Vector3.one;

    public Vector3 getForward(){
        Matrix3x3 matrix = Matrix3x3.getIdentity();
        matrix.RotateY(rotation.toVector3Rad().y);
        return matrix.TransformY(Vector3.forward);
    }

    private List<Vector3> getPointsInWorldCord(){
        List<Vector3> points = new ArrayList<Vector3>(){{
            add(new Vector3(-bound.x * 0.5f, 0f, bound.z * 0.5f));
            add(new Vector3(bound.x * 0.5f, 0f, bound.z * 0.5f));
            add(new Vector3(bound.x * 0.5f, 0f, -bound.z * 0.5f));
            add(new Vector3(-bound.x * 0.5f, 0f, -bound.z * 0.5f));
        }};
        Matrix3x3 matrix = Matrix3x3.getIdentity();
        matrix.RotateY(rotation.toVector3Rad().y);
        matrix.TranslateY(position.x, position.z);
        points = matrix.TransformY(points);

        return points;
    }

    public List<Vector3> getLocalPoint(List<Vector3> points)
    {
        Matrix3x3 matrix = Matrix3x3.getIdentity();
        for(int i = 0; i < points.size(); i++) points.set(i, points.get(i).subtract(position));

        matrix.RotateY(-rotation.toVector3Rad().y);
        points = matrix.TransformY(points);
        return points;
    }

    public boolean checkCollision(Transform other)
    {
        List<Vector3> points = getLocalPoint(other.getPointsInWorldCord());
        for(int i = 0; i < points.size(); i++)
        {
            Vector3 point = points.get(i);
            if (point.x <= bound.x * 0.5f && point.x >= -bound.x * 0.5f && point.z <= bound.z * 0.5f && point.z >= -bound.z * 0.5f) return true;
        }
        points = other.getLocalPoint(getPointsInWorldCord());
        for(int i = 0; i < points.size(); i++)
        {
            Vector3 point = points.get(i);
            if (point.x <= other.bound.x * 0.5f && point.x >= -other.bound.x * 0.5f && point.z <= other.bound.z * 0.5f && point.z >= -other.bound.z * 0.5f) return true;
        }
        return false;
    }
}
