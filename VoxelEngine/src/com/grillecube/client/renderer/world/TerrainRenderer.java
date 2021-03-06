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

package com.grillecube.client.renderer.world;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.grillecube.client.opengl.GLH;
import com.grillecube.client.opengl.GLTexture;
import com.grillecube.client.renderer.MainRenderer;
import com.grillecube.client.renderer.Renderer;
import com.grillecube.client.renderer.camera.CameraProjective;
import com.grillecube.client.renderer.camera.CameraView;
import com.grillecube.client.resources.BlockRendererManager;
import com.grillecube.common.Taskable;
import com.grillecube.common.VoxelEngine;
import com.grillecube.common.VoxelEngine.Callable;
import com.grillecube.common.maths.Vector3f;
import com.grillecube.common.resources.R;
import com.grillecube.common.world.WorldFlat;
import com.grillecube.common.world.terrain.WorldObjectTerrain;

public class TerrainRenderer extends Renderer {

	/** rendering program */
	private ProgramTerrain terrainProgram;
	private GLTexture breakAtlas;

	public TerrainRenderer(MainRenderer mainRenderer) {
		super(mainRenderer);
	}

	@Override
	public void initialize() {
		this.terrainProgram = new ProgramTerrain();
		this.breakAtlas = GLH.glhGenTexture(R.getResPath("textures/block_atlas/break.png"));
	}

	@Override
	public void deinitialize() {
		GLH.glhDeleteObject(this.terrainProgram);
		this.terrainProgram = null;
	}

	private final void bindTextureAtlas(TerrainMesh mesh, CameraView camera) {

		float distance = (float) Vector3f.distance(camera.getPosition(), mesh.getTerrain().getWorldPosition());
		BlockRendererManager manager = this.getMainRenderer().getResourceManager().getBlockTextureManager();
		GLTexture texture = null;

		if (distance < WorldObjectTerrain.SIZE_DIAGONAL3 * 1) {
			texture = manager.getTextureAtlas(BlockRendererManager.RESOLUTION_16x16);
		} else if (distance < WorldObjectTerrain.SIZE_DIAGONAL3 * 2.0f) {
			texture = manager.getTextureAtlas(BlockRendererManager.RESOLUTION_8x8);
		} else if (distance < WorldObjectTerrain.SIZE_DIAGONAL3 * 3.0f) {
			texture = manager.getTextureAtlas(BlockRendererManager.RESOLUTION_4x4);
		} else if (distance < WorldObjectTerrain.SIZE_DIAGONAL3 * 4.0f) {
			texture = manager.getTextureAtlas(BlockRendererManager.RESOLUTION_2x2);
		} else {
			texture = manager.getTextureAtlas(BlockRendererManager.RESOLUTION_1x1);
		}

		if (texture != null) {
			texture.bind(GL13.GL_TEXTURE0, GL11.GL_TEXTURE_2D);
		}
		this.breakAtlas.bind(GL13.GL_TEXTURE1, GL11.GL_TEXTURE_2D);

	}

	public void render(CameraProjective camera, WorldFlat world, ArrayList<TerrainMesh> opaqueMeshes,
			ArrayList<TerrainMesh> transparentMeshes) {

		GL11.glEnable(GL11.GL_DEPTH_TEST);

		if (this.getMainRenderer().getGLFWWindow().isKeyPressed(GLFW.GLFW_KEY_F)) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		}

		this.terrainProgram.useStart();
		this.terrainProgram.loadUniforms(camera, world);

		if (opaqueMeshes != null && opaqueMeshes.size() > 0) {
			this.drawMeshes(camera, opaqueMeshes, ProgramTerrain.MESH_TYPE_OPAQUE);
		}

		if (transparentMeshes != null && transparentMeshes.size() > 0) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			// bind textures
			this.drawMeshes(camera, transparentMeshes, ProgramTerrain.MESH_TYPE_TRANSPARENT);
			GL11.glDisable(GL11.GL_BLEND);
		}

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}

	private final void drawMeshes(CameraProjective camera, ArrayList<TerrainMesh> transparentMeshes, int meshType) {
		this.terrainProgram.loadTypeUniform(meshType);
		for (TerrainMesh mesh : transparentMeshes) {
			if (!mesh.isInitialized()) {
				continue;
			}
			this.bindTextureAtlas(mesh, camera);
			this.terrainProgram.loadInstanceUniforms(mesh);
			mesh.bind();
			mesh.draw();
		}
	}

	@Override
	public void getTasks(VoxelEngine engine, ArrayList<Callable<Taskable>> tasks) {
	}
}