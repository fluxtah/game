#include "../include/BulletDebugDrawer.h"
#include <iostream>

void BulletDebugDrawer::setDebugMode(int mode) {
    this->debugMode = mode;
}

int BulletDebugDrawer::getDebugMode() const {
    return debugMode;
}

void BulletDebugDrawer::drawLine(const btVector3 &from, const btVector3 &to, const btVector3 &color) {
   // std::cout << "Drawing line from " << from.getX() << ", " << from.getY() << ", " << from.getZ() << " to " << to.getX() << ", " << to.getY() << ", " << to.getZ() << std::endl;
    DebugVertex fromVertex = { { from.getX(), from.getY(), from.getZ() } };
    DebugVertex toVertex = { { to.getX(), to.getY(), to.getZ() } };

    debugVertices.push_back(fromVertex);
    debugVertices.push_back(toVertex);
    vertexCount += 2;
    // std::cout << "Drawing lines: " << debugVertices.size() << std::endl;
}

void BulletDebugDrawer::drawContactPoint(const btVector3 &PointOnB, const btVector3 &normalOnB, btScalar distance,
                                         int lifeTime, const btVector3 &color) {
    //std::cout << "Drawing contact point" << std::endl;
}

void BulletDebugDrawer::reportErrorWarning(const char *warningString) {
    std::cerr << "Bullet warning: " << warningString << std::endl;
}

void BulletDebugDrawer::draw3dText(const btVector3 &location, const char *textString) {
   // std::cout << "Drawing 3D text at " << location.getX() << ", " << location.getY() << ", " << location.getZ() << ": " << textString << std::endl;
}

void BulletDebugDrawer::clearLines() {
    debugVertices.clear();
    vertexCount = 0;
}

void *BulletDebugDrawer::getVertexData() {
    return debugVertices.data();
}

int BulletDebugDrawer::getVertexCount() const {
    return vertexCount;
}
