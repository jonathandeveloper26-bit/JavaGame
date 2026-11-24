package ripplepond.model.geometry;

import java.util.Objects;
/**
 * Represents an immutable two-dimensional vector with an X and Y coordinate.
 * <p>
 * This class provides basic vector operations such as addition, subtraction,
 * scaling, normalization, and distance calculation. It can be used to
 * represent positions, directions, and movements in a two-dimensional space.
 * <p>
 * Since the class is immutable, its coordinates cannot be modified after
 * construction. Operations return new {@code Vec2} instances instead.
 */
public final class Vec2 {
    private final double x;
    private final double y;

    /**
     * Creates a new vector with the specified coordinates.
     *
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     */
    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the horizontal coordinate of this vector.
     *
     * @return the X coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the vertical coordinate of this vector.
     *
     * @return the Y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Returns a new vector representing the sum of this vector and another.
     *
     * @param other the vector to add
     * @return a new vector containing the sum of both vectors
     */
    public Vec2 plus(Vec2 other){
        return new Vec2(this.x + other.getX(), this.y + other.getY());
    }

    /**
     * Returns a new vector representing the difference between this vector
     * and another vector.
     *
     * @param other the vector to subtract
     * @return a new vector containing the difference
     */
    public Vec2 minus(Vec2 other){
        return new Vec2(this.x - other.getX(), this.y - other.getY());
    }

    /**
     * Returns a new vector obtained by multiplying both coordinates
     * by the specified factor.
     *
     * @param factor the multiplication factor
     * @return a new scaled vector
     */
    public Vec2 scale(double factor) {
        return new Vec2(this.x * factor, this.y * factor);
    }

    /**
     * Calculates the Euclidean length (magnitude) of this vector.
     *
     * @return the length of the vector
     */
    public double length(){
        return Math.hypot(this.x, this.y);
    }

    /**
     * Returns a normalized version of this vector, preserving its direction
     * while giving it a length of 1.
     * <p>
     * If the vector has zero length, the current instance is returned
     * to avoid division by zero.
     *
     * @return a new normalized vector, or this vector if its length is zero
     */
    public Vec2 normalised() {
        double len = length();
        if (len == 0) {
            return this;
        }
        return new Vec2(x / len, y / len);
    }

    /**
     * Calculates the Euclidean distance between this vector and another.
     *
     * @param other the vector to measure the distance to
     * @return the distance between the two vectors
     */
    public double distanceTo(Vec2 other) {
        return minus(other).length();
    }

    /**
     * Moves this vector towards a target by at most the specified distance.
     * <p>
     * If the target is closer than or equal to the maximum step,
     * the target itself is returned. Otherwise, a new vector is calculated
     * in the direction of the target.
     * <p>
     * A negative maximum step is not allowed and results in an exception.
     *
     * @param target the destination vector
     * @param maxStep the maximum distance to move; must not be negative
     * @return the resulting position after the movement
     * @throws IllegalArgumentException if {@code maxStep} is negative
     */
    public Vec2 stepTowards(Vec2 target, double maxStep) {
        if (maxStep < 0) {
            throw new IllegalArgumentException("maxStep must not be negative: " + maxStep);
        }
        Vec2 offset = target.minus(this);
        if (offset.length() <= maxStep) {
            return target;
        }
        return plus(offset.normalised().scale(maxStep));
    }

    /** The dot product: large when two vectors point the same way, zero when they are at right angles. */
    public double dot(Vec2 other) {
        return x * other.x + y * other.y;
    }

    /**
     * Checks whether this vector is equal to another object.
     * <p>
     * Two vectors are considered equal if both their X and Y coordinates
     * are equal according to {@link Double#compare(double, double)}.
     *
     * @param o the object to compare with
     * @return {@code true} if both vectors have identical coordinates,
     *         {@code false} otherwise
     */
    @Override public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || !(o instanceof Vec2) ) return false;
        Vec2 vec2 = (Vec2) o;
        return Double.compare(vec2.x, x) == 0 && Double.compare(vec2.y, y) == 0;
    }

    /**
     * Returns a hash code based on the vector's coordinates.
     *
     * @return the hash code of this vector
     */
    @Override public int hashCode(){
        return Objects.hash(x, y);
    }

    /**
     * Returns a string representation of this vector in the format
     * {@code (x, y)}.
     *
     * @return the string representation of this vector
     */
    @Override public String toString(){
        return "(" + x + ", " + y + ")";
    }
}
