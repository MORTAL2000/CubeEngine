package com.grillecube.client.renderer.model.editor.mesher;

import java.util.HashMap;

import com.grillecube.client.renderer.model.ModelSkin;
import com.grillecube.common.utils.Color;

/** hold the data of a single block of the model */
public class ModelBlockData {

	/** the plan linked to this block */
	private final String[] bones;
	private final float[] weights;
	private final int x, y, z;
	private final HashMap<ModelSkin, Color> colors;

	public ModelBlockData(int bx, int by, int bz) {
		this.x = bx;
		this.y = by;
		this.z = bz;
		this.bones = new String[] { "", "", "" };
		this.weights = new float[] { 1, 0, 0 };
		this.colors = new HashMap<ModelSkin, Color>();
	}

	public final Color getColor(ModelSkin modelSkin) {
		return (this.colors.containsKey(modelSkin) ? this.colors.get(modelSkin) : Color.GRAY);
	}

	public final void setColor(ModelSkin modelSkin, Color color) {
		this.colors.put(modelSkin, color);
	}

	public final String getBone(int i) {
		return (this.bones[i]);
	}

	public final float getBoneWeight(int i) {
		return (this.weights[i]);
	}

	public final float getBoneWeight(String bone) {
		for (int i = 0; i < this.bones.length; i++) {
			if (this.bones.equals(bone)) {
				return (this.weights[i]);
			}
		}
		return (0);
	}

	public final void setBone(int i, String bone, float weight) {
		this.bones[i] = bone;
		this.weights[i] = weight;
	}

	public final int getX() {
		return (this.x);
	}

	public final int getY() {
		return (this.y);
	}

	public final int getZ() {
		return (this.z);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("BlockData{");
		sb.append(this.bones[0]);
		sb.append(",");
		sb.append(this.bones[1]);
		sb.append(",");
		sb.append(this.bones[2]);
		sb.append(",");
		sb.append(this.weights[0]);
		sb.append(",");
		sb.append(this.weights[1]);
		sb.append(",");
		sb.append(this.weights[2]);
		sb.append("}");
		return (sb.toString());
	}
}