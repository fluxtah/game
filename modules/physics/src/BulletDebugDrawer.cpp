#include "../include/BulletDebugDrawer.h"
#include <iostream>

void BulletDebugDrawer::setDebugMode(int debugMode) {
    m_debugMode = debugMode;
}

int BulletDebugDrawer::getDebugMode() const {
    return m_debugMode;
}

void BulletDebugDrawer::drawLine(const btVector3 &from, const btVector3 &to, const btVector3 &color) {
   // std::cout << "Drawing line from " << from.getX() << ", " << from.getY() << ", " << from.getZ() << " to " << to.getX() << ", " << to.getY() << ", " << to.getZ() << std::endl;
}

void BulletDebugDrawer::drawContactPoint(const btVector3 &PointOnB, const btVector3 &normalOnB, btScalar distance,
                                         int lifeTime, const btVector3 &color) {
std::cout << "Drawing contact point" << std::endl;
}

void BulletDebugDrawer::reportErrorWarning(const char *warningString) {
    std::cerr << "Bullet warning: " << warningString << std::endl;
}

void BulletDebugDrawer::draw3dText(const btVector3 &location, const char *textString) {
    std::cout << "Drawing 3D text at " << location.getX() << ", " << location.getY() << ", " << location.getZ() << ": " << textString << std::endl;
}
