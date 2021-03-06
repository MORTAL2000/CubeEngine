/**
**	This file is part of the project https://github.com/toss-dev/VoxelEngine
**
**	License is available here: https://raw.githubusercontent.com/toss-dev/VoxelEngine/master/LICENSE.md
**
**	PEREIRA Romain
**                                       4-----7          
**                                      /|    /|
**                                     0-----3 |
**                                     | 5___|_6
**                                     |/    | /
**                                     1-----2
*/

package com.grillecube.client.renderer;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.grillecube.client.opengl.GLH;
import com.grillecube.client.opengl.GLVertexArray;
import com.grillecube.client.opengl.GLVertexBuffer;
import com.grillecube.client.renderer.camera.Camera;
import com.grillecube.client.renderer.world.TerrainMesher;
import com.grillecube.common.Logger;
import com.grillecube.common.maths.Matrix4f;
import com.grillecube.common.maths.Vector3f;

public abstract class Mesh {

	/** terrain meshes */
	private int vertexCount;

	/** opengl */
	protected GLVertexArray vao;
	protected GLVertexBuffer vbo;

	private Vector3f rotation;
	private Vector3f position;
	private Vector3f scale;

	private Matrix4f transfMatrix;

	public Mesh() {
		this.transfMatrix = new Matrix4f();
		this.rotation = new Vector3f();
		this.position = new Vector3f();
		this.scale = new Vector3f(1.0f, 1.0f, 1.0f);
		this.updateTransformationMatrix();
	}

	/** initialize opengl stuff (vao, vbo) */
	public void initialize() {

		if (this.isInitialized()) {
			Logger.get().log(Logger.Level.WARNING, "tried to initialized a mesh that was already initialized!!");
			return;
		}

		this.vao = GLH.glhGenVAO();
		this.vbo = GLH.glhGenVBO();

		this.vao.bind();
		this.vbo.bind(GL15.GL_ARRAY_BUFFER);

		this.setAttributes(this.vao, this.vbo);

		// this.vbo.unbind(GL15.GL_ARRAY_BUFFER);
		// this.vao.unbind();
	}

	public final boolean isInitialized() {
		return (this.vao != null);
	}

	public final void deinitialize() {
		GLH.glhDeleteObject(this.vao);
		GLH.glhDeleteObject(this.vbo);

		this.vao = null;
		this.vbo = null;

		this.onDeinitialized();
	}

	protected void onDeinitialized() {
	}

	protected abstract void setAttributes(GLVertexArray vao, GLVertexBuffer vbo);

	public final GLVertexArray getVAO() {
		return (this.vao);
	}

	public final GLVertexBuffer getVBO() {
		return (this.vbo);
	}

	public final ByteBuffer getVertices() {
		if (this.vbo == null) {
			return (null);
		}
		return (this.vbo.getContent(0));
	}

	public void bind() {
		if (!this.isInitialized()) {
			this.initialize();
		}
		this.vao.bind();
	}

	/** called in the rendering thread */
	public final void draw() {
		this.preDraw();
		this.vao.draw(GL11.GL_TRIANGLES, 0, this.vertexCount);
		this.postDraw();
	}

	protected void preDraw() {
	}

	protected void postDraw() {
	}

	/** draw with index buffer */
	public final void drawElements(int indexCount, int indiceType) {
		GLH.glhDrawElements(GL11.GL_TRIANGLES, indexCount, indiceType, 0);
	}

	public final void drawInstanced(int primcount) {
		this.vao.drawInstanced(GL11.GL_TRIANGLES, 0, this.vertexCount, primcount);
	}

	public final int getVertexCount() {
		return (this.vertexCount);
	}

	/** returnt rue if the mesh has been regenerated */
	public boolean update(TerrainMesher mesher, Camera camera) {
		return (false);
	}

	protected final void setVertices(ByteBuffer buffer, int bytesPerVertex) {
		if (buffer == null || buffer.capacity() == 0) {
			if (this.isInitialized()) {
				this.deinitialize();
			}
			return;
		}

		if (!this.isInitialized()) {
			this.initialize();
		}
		this.vertexCount = buffer.capacity() / bytesPerVertex;
		this.vbo.bind(GL15.GL_ARRAY_BUFFER);
		this.vbo.bufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}

	protected final void updateTransformationMatrix() {
		Matrix4f.createTransformationMatrix(this.transfMatrix, this.getPosition(), this.getRotation(), this.getScale());
	}

	public final Vector3f getPosition() {
		return (this.position);
	}

	public final Vector3f getRotation() {
		return (this.rotation);
	}

	public final Vector3f getScale() {
		return (this.scale);
	}

	public final void setPosition(float x, float y, float z) {
		this.position.set(x, y, z);
		this.updateTransformationMatrix();
	}

	public final void setRotation(float x, float y, float z) {
		this.rotation.set(x, y, z);
		this.updateTransformationMatrix();
	}

	public final void setScale(float x, float y, float z) {
		this.scale.set(x, y, z);
		this.updateTransformationMatrix();
	}

	public final Matrix4f getTransformationMatrix() {
		return (this.transfMatrix);
	}
}