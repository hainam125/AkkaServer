package games;

import games.transform.Vector3;

import java.util.List;

public class Matrix3x3 {
    public static Matrix3x3 getIdentity() {
        return new Matrix3x3();
    }
    private class Matrix {
        public float _11, _12, _13;
        public float _21, _22, _23;
        public float _31, _32, _33;

        public Matrix() {
            _11 = 0.0f; _12 = 0.0f; _13 = 0.0f;
            _21 = 0.0f; _22 = 0.0f; _23 = 0.0f;
            _31 = 0.0f; _32 = 0.0f; _33 = 0.0f;
        }
    }

    private Matrix m_Matrix = new Matrix();

    private Matrix3x3() {
        Identity();
    }

    private void MatrixMultiply(Matrix mIn) {
        Matrix mat_temp = new Matrix();
        //first row
        mat_temp._11 = (m_Matrix._11 * mIn._11) + (m_Matrix._12 * mIn._21) + (m_Matrix._13 * mIn._31);
        mat_temp._12 = (m_Matrix._11 * mIn._12) + (m_Matrix._12 * mIn._22) + (m_Matrix._13 * mIn._32);
        mat_temp._13 = (m_Matrix._11 * mIn._13) + (m_Matrix._12 * mIn._23) + (m_Matrix._13 * mIn._33);

        //second
        mat_temp._21 = (m_Matrix._21 * mIn._11) + (m_Matrix._22 * mIn._21) + (m_Matrix._23 * mIn._31);
        mat_temp._22 = (m_Matrix._21 * mIn._12) + (m_Matrix._22 * mIn._22) + (m_Matrix._23 * mIn._32);
        mat_temp._23 = (m_Matrix._21 * mIn._13) + (m_Matrix._22 * mIn._23) + (m_Matrix._23 * mIn._33);

        //third
        mat_temp._31 = (m_Matrix._31 * mIn._11) + (m_Matrix._32 * mIn._21) + (m_Matrix._33 * mIn._31);
        mat_temp._32 = (m_Matrix._31 * mIn._12) + (m_Matrix._32 * mIn._22) + (m_Matrix._33 * mIn._32);
        mat_temp._33 = (m_Matrix._31 * mIn._13) + (m_Matrix._32 * mIn._23) + (m_Matrix._33 * mIn._33);

        m_Matrix = mat_temp;
    }

    public void Identity() {
        m_Matrix._11 = 1; m_Matrix._12 = 0; m_Matrix._13 = 0;
        m_Matrix._21 = 0; m_Matrix._22 = 1; m_Matrix._23 = 0;
        m_Matrix._31 = 0; m_Matrix._32 = 0; m_Matrix._33 = 1;
    }

    public void TranslateY(float x, float z) {
        Matrix mat = new Matrix();

        mat._11 = 1; mat._12 = 0; mat._13 = 0;
        mat._21 = x; mat._22 = 1; mat._23 = z;
        mat._31 = 0; mat._32 = 0; mat._33 = 1;

        MatrixMultiply(mat);
    }

    public void ScaleY(float xScale, float zScale) {
        Matrix mat = new Matrix();

        mat._11 = xScale; mat._12 = 0; mat._13 = 0;
        mat._21 = 0; mat._22 = 1; mat._23 = 0;
        mat._31 = 0; mat._32 = 0; mat._33 = zScale;

        MatrixMultiply(mat);
    }

    public void RotateY(float rot) {
        Matrix mat = new Matrix();

        float SinY = (float)Math.sin(rot);
        float CosY = (float)Math.cos(rot);

        mat._11 = CosY; mat._21 = 0; mat._31 = SinY;
        mat._12 = 0; mat._22 = 1; mat._32 = 0;
        mat._13 = -SinY; mat._23 = 0; mat._33 = CosY;

        MatrixMultiply(mat);
    }

    public void RotateY(Vector3 fwd, Vector3 side) {
        Matrix mat = new Matrix();

        mat._11 = fwd.x; mat._21 = 0; mat._31 = fwd.z;
        mat._12 = 0; mat._22 = 1; mat._32 = 0;
        mat._13 = side.x; mat._23 = 0; mat._33 = side.z;

        MatrixMultiply(mat);
    }

    public Vector3 TransformY(Vector3 vPoint) {
        float tempX = m_Matrix._11 * vPoint.x + m_Matrix._31 * vPoint.z + m_Matrix._21;
        float tempZ = m_Matrix._13 * vPoint.x + m_Matrix._33 * vPoint.z + m_Matrix._23;
        return new Vector3(tempX, 0, tempZ);
    }

    public List<Vector3> TransformY(List<Vector3> vPoints)
    {
        for (int i = 0; i < vPoints.size(); i++)
        {
            float tempX = m_Matrix._11 * vPoints.get(i).x + m_Matrix._31 * vPoints.get(i).z + m_Matrix._21;
            float tempZ = m_Matrix._13 * vPoints.get(i).x + m_Matrix._33 * vPoints.get(i).z + m_Matrix._23;
            vPoints.set(i, new Vector3(tempX, 0, tempZ));
        }
        return vPoints;
    }
}
