// This is the file from the github repo

package jme3project.sagacity;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Node;
import com.jme3.scene.CameraNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LightControl;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sagacity extends SimpleApplication implements AnimEventListener
{
    // Class variables //
    // -------------------------- //

    private Random randomGenerator = new Random();
    private Node camNode;
    private Node[] rooms;
    private Node currentNode = rootNode; // for debugging camera
    private Node player;
    private CollisionResults results;
    private DirectionalLight sunlight;
    private AmbientLight ambient;
    private PointLight roomLight;
    private int minRooms = 10;
    private int maxRooms = 20;
    private int roomGridWidth = 14;
    private int roomGridHeight = 10;
    private int wallCollisionIndex = 0;
    private int environmentCollisionIndex = 0;
    private int enemyCollisionIndex = 0;
    private int randomEnemyCollisionIndex = 0;
    private int enemyXRotation = 0;
    private int enemyZRotation = 0;
    private int enemyMovementIndex = 0;
    private int textIndex = 0;
    private int enemyChance = 40;
    private int shottimer = 0;
    private float blockWidth = 5;
    private float blockHeight = 5;
    private BulletAppState bulletAppState = new BulletAppState();
    private RigidBodyControl wallCollision[];
    private RigidBodyControl floorCollision[];
    private RigidBodyControl environmentCollision[];
    private BetterCharacterControl enemyCollision[];
    private BetterCharacterControl randomEnemyCollision[];
    private BetterCharacterControl bossCollision;
    private RigidBodyControl playerCollision;
    private BetterCharacterControl playerControl;
    private Quaternion rotation = new Quaternion();
    private boolean gameOver = false;
    private boolean atTitleScreen = true;
    private boolean menuOpen = false;
    private boolean merchantOpen = false;
    private boolean metMerchant = false;
    private boolean gaveMerchant = false;
    private boolean wisdomOpen = false;
    private boolean bossTest = false;
    private boolean unlockEnemies = false;
    private int wisdomNumber = -1;
    private boolean[] wisdomUnlock = {false, false, false, false};
    private Player sage = new Player();
    private Camera camera = new Camera(rootNode);
    
    private AnimChannel animationChannel;
    private AnimControl animationControl;
    
    private Vector3f walkingDirection = new Vector3f(0,0,0);
    private Vector3f puppetWalkingDirection = new Vector3f(0,0,0);
    private Vector3f randomWalkingDirection = new Vector3f(0,0,0);
    
    private AudioNode runningSound;
    private AudioNode healingSound;
    private AudioNode startGameSound;
    private AudioNode endGameSound;
    private AudioNode useItemSound;
    private AudioNode windSound;
    
    private int tempEnemyHealth = 50;

    // -------------------------- //
    public static void main(String[] args)
    {
        Sagacity app = new Sagacity();

        app.start();
    }

    // Runs at startup of the application
    @Override
    public void simpleInitApp()
    {
        initKeys();
        initSounds();
        displayTitleScreen();
    }
    
    protected void startGame()
    {
        stateManager.attach(bulletAppState);
        //bulletAppState.setDebugEnabled(true);
        makeFloor();
        makeCharacterController();
        for(boolean wisdom : wisdomUnlock)
        {
            if(wisdom == false)
            {
                makeWisdom();
                break;
            }
        }
        makePowerUp();
        makeDefenseUp();
        
        makeEnvironment();
        if(unlockEnemies)
        {
            makeEnemies();
        }
        setupCamera(rootNode, 0, 750, 35);
        camera.setY(65);
        camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
        initLight();
    }
    
    protected void resetGame()
    {
        rootNode.detachAllChildren();
        rootNode.removeLight(ambient);
        rootNode.removeLight(roomLight);
        stateManager.detach(bulletAppState);
        sage = new Player(sage.getAttack(), sage.getDefense(), sage.getBliss(), sage.getStorytelling());
        camera = new Camera(rootNode);
        gaveMerchant = false;
        enemyCollisionIndex = 0;
        randomEnemyCollisionIndex = 0;
        initSounds();
    }
    
    protected void displayTitleScreen()
    {
        guiNode.detachAllChildren();
        
        Geometry titleScreen = new Geometry("TitleScreen", new Quad(getScreenWidth(), getScreenHeight()));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        titleScreen.setMaterial(mat);
        titleScreen.setLocalTranslation(0, 0, 0);
        
        Geometry titleBox = new Geometry("TitleBox", new Quad(getScreenWidth() / 1.7f, getScreenHeight() / 1.7f));
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.White);
        Texture title = assetManager.loadTexture("Textures/PoE.png");
        mat2.setTexture("ColorMap", title);
        titleBox.setMaterial(mat2);
        titleBox.setLocalTranslation(getScreenWidth() / 4.5f, getScreenHeight() / 3.9f, 0);
        
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText continueText = new BitmapText(guiFont, false);
        continueText.setColor(ColorRGBA.LightGray);
        continueText.setSize(guiFont.getCharSet().getRenderedSize());
        continueText.setText("Press SPACE to continue...");
        continueText.setLocalTranslation(getScreenWidth() / 2.4f, getScreenHeight() / 4.2f, 0);
        
        guiNode.attachChild(titleScreen);
        guiNode.attachChild(titleBox);
        guiNode.attachChild(continueText);
    }
    
    protected void displayGameOver()
    {
        // Removing all children so the screen only displays GAME OVER
        guiNode.detachAllChildren();
        
        Geometry gameOverScreen = new Geometry("GameOverScreen", new Quad(getScreenWidth(), getScreenHeight()));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Black);
        gameOverScreen.setMaterial(mat);
        gameOverScreen.setLocalTranslation(0, 0, 0);
        
        
        // placing the GAME OVER message approximately in the center of the screen
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText gameOverText = new BitmapText(guiFont, false);
        gameOverText.setSize(guiFont.getCharSet().getRenderedSize());
        gameOverText.setText("GAME OVER");
        gameOverText.setLocalTranslation(getScreenWidth() / 2 - 20, getScreenHeight() / 2, 0);
        
        guiNode.attachChild(gameOverScreen);
        guiNode.attachChild(gameOverText);
    }
    
    private void displayMenu()
    {
        Geometry background = new Geometry("MenuBackground", new Quad(getScreenWidth() * (2.0f/3.0f), getScreenHeight() * (2.0f/3.0f)));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0,0.5f,0.5f,0.75f));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        background.setMaterial(mat);
        background.setLocalTranslation(getScreenWidth() * (0.5f/3.0f), getScreenHeight() * (0.5f/3.0f), 0);
        
        Geometry border = new Geometry("MenuBorder", new Quad(getScreenWidth() * (1.90f/3.0f), getScreenHeight() * (1.90f/3.0f)));
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", new ColorRGBA(0,0.5f,0.75f,0.75f));
        mat2.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        border.setMaterial(mat2);
        border.setLocalTranslation(getScreenWidth() * (0.55f/3.0f), getScreenHeight() * (0.55f/3.0f), 0);
        
        Geometry foreground = new Geometry("MenuBorder", new Quad(getScreenWidth() * (1.80f/3.0f), getScreenHeight() * (1.80f/3.0f)));
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", new ColorRGBA(0,0.01f,0.05f,0.55f));
        mat3.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        foreground.setMaterial(mat3);
        foreground.setLocalTranslation(getScreenWidth() * (0.60f/3.0f), getScreenHeight() * (0.60f/3.0f), 0);
        
        
       
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText nameText = new BitmapText(guiFont, false);
        nameText.setSize(24);
        nameText.setText(     "Name:\n"
                            + "Life:\n"
                            + "Power:\n"
                            + "Defense:\n"
                            + "Bliss:\n"
                            + "Storytelling:\n");
        nameText.setLocalTranslation(getScreenWidth() * (0.62f/3.0f), getScreenHeight() * (2.36f/3.0f), 0);
        
        BitmapText statText = new BitmapText(guiFont, false);
        statText.setSize(24);
        statText.setText(     "Kid #1\n"
                            + sage.getHealth() + "/100\n"
                            + sage.getAttack() + "\n"
                            + sage.getDefense() + "\n"
                            + sage.getBliss() + "\n"
                            + sage.getStorytelling());
        statText.setLocalTranslation(getScreenWidth() * (0.92f/3.0f), getScreenHeight() * (2.36f/3.0f), 0);
        
        BitmapText merchantText = new BitmapText(guiFont, false);
        merchantText.setSize(16);
        merchantText.setColor(ColorRGBA.LightGray);
        merchantText.setText("Spirit Merchant(L)");
        merchantText.setLocalTranslation(getScreenWidth() * (0.62f/3.0f), getScreenHeight() * (0.70f/3.0f), 0);
        
        guiNode.attachChild(background);
        guiNode.attachChild(border);
        guiNode.attachChild(foreground);
        guiNode.attachChild(nameText);
        guiNode.attachChild(statText);
        guiNode.attachChild(merchantText);
    }
    
    protected void displayMerchant()
    {
        Geometry background = new Geometry("MenuBackground", new Quad(getScreenWidth() * (2.0f/3.0f), getScreenHeight() * (2.0f/3.0f)));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0,0.5f,0.5f,0.75f));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        background.setMaterial(mat);
        background.setLocalTranslation(getScreenWidth() * (0.5f/3.0f), getScreenHeight() * (0.5f/3.0f), 0);
        
        Geometry border = new Geometry("MenuBorder", new Quad(getScreenWidth() * (1.90f/3.0f), getScreenHeight() * (1.90f/3.0f)));
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", new ColorRGBA(0,0.5f,0.75f,0.75f));
        mat2.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        border.setMaterial(mat2);
        border.setLocalTranslation(getScreenWidth() * (0.55f/3.0f), getScreenHeight() * (0.55f/3.0f), 0);
        
        Geometry foreground = new Geometry("MenuBorder", new Quad(getScreenWidth() * (1.80f/3.0f), getScreenHeight() * (1.80f/3.0f)));
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", new ColorRGBA(0,0.01f,0.05f,0.55f));
        mat3.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        foreground.setMaterial(mat3);
        foreground.setLocalTranslation(getScreenWidth() * (0.60f/3.0f), getScreenHeight() * (0.60f/3.0f), 0);
        
        Geometry merchant = new Geometry("merchant", new Quad(getScreenWidth() * (0.60f/3.0f), getScreenHeight() * (0.90f/3.0f)));
        Material mat4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture merchantTexture = assetManager.loadTexture("Textures/SpiritMerchant.png"); // Texture needs work - shows up black
        mat4.setTexture("ColorMap", merchantTexture);
        merchant.setMaterial(mat4);
        merchant.setLocalTranslation(getScreenWidth() * (1.1f/3.0f), getScreenHeight() * (1.2f/3.0f), 0);
        
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText speechText = new BitmapText(guiFont, false);
        speechText.setSize(24);
        if(textIndex == 0)
        {
            speechText.setText("Ah! Oh no...");
        }
        else if(textIndex == 1)
        {
            speechText.setText("You weren't supposed to find me.");
        }
        else if(textIndex == 2)
        {
            speechText.setText("Well, since you're here, do you have any of those delicious cubes with you?");
        }
        else if(textIndex == 3)
        {
            speechText.setText("You know, the blue and green ones!");
        }
        else if(textIndex == 4)
        {
            speechText.setText("I just love those. Oh, and I'll give you something good for them! I promise!");
        }
        else if(metMerchant)
        {
            if(gaveMerchant)
            {
                speechText.setText("Oh th-thank you so much! Here, take this!");
            }
            else
            {
                speechText.setText("J-just, press N to give me a blue one, or M to give me a green one.");
            }
        }
        speechText.setLocalTranslation(getScreenWidth() * (0.62f/3.0f), getScreenHeight() * (1.0f/3.0f), 0);
        
        guiNode.attachChild(background);
        guiNode.attachChild(border);
        guiNode.attachChild(foreground);
        guiNode.attachChild(merchant);
        guiNode.attachChild(speechText);
    }
    
    protected void displayWisdom()
    {
        Geometry background = new Geometry("MenuBackground", new Quad(getScreenWidth() * (2.0f/3.0f), getScreenHeight() * (2.0f/3.0f)));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0,0.5f,0.5f,0.75f));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        background.setMaterial(mat);
        background.setLocalTranslation(getScreenWidth() * (0.5f/3.0f), getScreenHeight() * (0.5f/3.0f), 0);
        
        Geometry border = new Geometry("MenuBorder", new Quad(getScreenWidth() * (1.90f/3.0f), getScreenHeight() * (1.90f/3.0f)));
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", new ColorRGBA(0,0.5f,0.75f,0.75f));
        mat2.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        border.setMaterial(mat2);
        border.setLocalTranslation(getScreenWidth() * (0.55f/3.0f), getScreenHeight() * (0.55f/3.0f), 0);
        
        Geometry foreground = new Geometry("MenuBorder", new Quad(getScreenWidth() * (1.80f/3.0f), getScreenHeight() * (1.80f/3.0f)));
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", new ColorRGBA(0,0.01f,0.05f,0.55f));
        mat3.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        foreground.setMaterial(mat3);
        foreground.setLocalTranslation(getScreenWidth() * (0.60f/3.0f), getScreenHeight() * (0.60f/3.0f), 0);
        
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText wisdomText = new BitmapText(guiFont, false);
        wisdomText.setSize(24);
        if(wisdomNumber == 0 && wisdomUnlock[wisdomNumber] == false)
        {
            //wisdomUnlock[wisdomNumber] = true;
            wisdomText.setText("The CoDE\n"
                            + "Mary,\n"
                            + "Mary,\n"
                            + "Paul.\n\n"
                            + "MMP\n");
        }
        if(wisdomNumber == 1 && wisdomUnlock[wisdomNumber] == false)
        {
            //wisdomUnlock[wisdomNumber] = true;
            wisdomText.setText("How many times must you be told?\n"
                            + "...\n"
                            + "I said drain it completely, every time.\n"
                            + "...\n\n"
                            + "You have nothing to say for yourself, as usual.\n");
        }
        if(wisdomNumber == 2 && wisdomUnlock[wisdomNumber] == false)
        {
            //wisdomUnlock[wisdomNumber] = true;
            wisdomText.setText("Quickly, this way!\n"
                            + "Have you got the amulet?\n"
                            + "O-of course I do!\n"
                            + "Good, all may not be lost.\n"
                            + "When you get to the end, take the left door, do you understand?\n\n"
                            + "The left, not the right.\n\n");
        }
        if(wisdomNumber == 3 && wisdomUnlock[wisdomNumber] == false)
        {
            //wisdomUnlock[wisdomNumber] = true;
            wisdomText.setText("You know, things weren't always this way.\n"
                            + "Really?\n"
                            + "Really.\n"
                            + "She used to fancy me quite a bit actually.\n"
                            + "What happened?\n\n"
                            + "Ah, that is a story for another time, come along.\n\n");
        }
        wisdomText.setLocalTranslation(getScreenWidth() * (0.62f/3.0f), getScreenHeight() * (2.36f/3.0f), 0);
        
        guiNode.attachChild(background);
        guiNode.attachChild(border);
        guiNode.attachChild(foreground);
        guiNode.attachChild(wisdomText);
    }
    
    protected void makeCharacterController()
    {
        results = new CollisionResults();
        player = new Node();
        sage.setNode(player);
        sage.getNode().setLocalTranslation(rootNode.getLocalTranslation());
        sage.getNode().setName("PlayerNode");
        
        Spatial playerBox = (Node)assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        Node animationNode = (Node) playerBox;
        playerBox.move(0f, 6f, 0f);
        playerBox.setName("Player");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture color = assetManager.loadTexture("Textures/ShortBoyColor.png"); // Texture needs work - shows up black
        mat.setTexture("ColorMap", color);
        playerBox.setMaterial(mat);
        sage.getNode().attachChild(playerBox);
        rootNode.attachChild(sage.getNode());
        
        animationControl = animationNode.getControl(AnimControl.class);
        animationControl.addListener(this);
        animationChannel = animationControl.createChannel();
        animationChannel.setAnim("stand");
        animationChannel.setLoopMode(LoopMode.Loop);
        //for (String anim : animationControl.getAnimationNames()) { System.out.println(anim); }
        
        // Width, height, weight for BCC params
        playerControl = new BetterCharacterControl(1.5f, 12f, 8000f);
        sage.getNode().addControl(playerControl);
        
        bulletAppState.getPhysicsSpace().add(playerControl); 
        bulletAppState.getPhysicsSpace().addAll(sage.getNode());
        bulletAppState.getPhysicsSpace().addCollisionListener(sage); 
    }
    
    // wisp
    protected void makeWisp(Node room, double xLocation, double zLocation)
    {
        Node enemyNode = new Node();
        room.attachChild(enemyNode);
        enemyNode.setName("EnemyNode");
        Sphere enemySphere = new Sphere(32,32,2f);
        Spatial enemyBox = new Geometry("Enemy", enemySphere);
        //Spatial enemyBox = assetManager.loadModel("Models/EnemyJointless/EnemyJointless.j3o");
        enemyBox.setLocalTranslation((float)xLocation, 2f, (float)zLocation);
        enemyBox.setName("Enemy");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(.5f,.5f,.5f,.5f));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        enemyBox.setMaterial(mat);
        enemyNode.attachChild(enemyBox);
        
        RigidBodyControl enemyCollision = new RigidBodyControl(1.0f);
        enemyBox.addControl(enemyCollision);
        
        bulletAppState.getPhysicsSpace().add(enemyCollision);
        
        
    }
    
    // make unjointed puppet - should be implemented better, given more time this would improve
    protected void makePuppet(Node room, double xLocation, double zLocation, String model, String texture)
    {
        Spatial enemy = assetManager.loadModel("Models/" + model);
        
        room.attachChild(enemy);
        
        enemy.setName("Enemy");
        enemy.setUserData("health", 200);
        enemy.setLocalTranslation((float) xLocation, 2f, (float) zLocation);
        //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //mat.setColor("Color", ColorRGBA.Brown);
        
        //enemy.setMaterial((Material) assetManager.loadMaterial("Materials/Rock_1.j3m"));
        
        enemyCollision[enemyCollisionIndex] = new BetterCharacterControl(0.5f, 6f, 1000f);
        enemy.addControl(enemyCollision[enemyCollisionIndex]);
        bulletAppState.getPhysicsSpace().add(enemyCollision[enemyCollisionIndex]);
      
        enemyCollisionIndex++;
    }
    
    // Uses special collision for random movement
    protected void makeSpinningEnemy(Node room, double xLocation, double zLocation, String model, String texture)
    {
        Spatial enemy = assetManager.loadModel("Models/" + model);
        
        room.attachChild(enemy);
        
        enemy.setName("Enemy");
        enemy.setUserData("health", 200);
        enemy.setLocalTranslation((float) xLocation, 2f, (float) zLocation);
        //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //mat.setColor("Color", ColorRGBA.Brown);
        
        //enemy.setMaterial((Material) assetManager.loadMaterial("Materials/Rock_1.j3m"));
        
        randomEnemyCollision[randomEnemyCollisionIndex] = new BetterCharacterControl(0.5f, 6f, 1000f);
        enemy.addControl(randomEnemyCollision[randomEnemyCollisionIndex]);
        bulletAppState.getPhysicsSpace().add(randomEnemyCollision[randomEnemyCollisionIndex]);
      
        randomEnemyCollisionIndex++;
    }
    
    protected void makeBoss(Node room, double xLocation, double zLocation, String model, String texture)
    {
        Spatial boss = assetManager.loadModel("Models/" + model);
        
        room.attachChild(boss);
        
        boss.setName("Boss");
        boss.setUserData("health", 5000);
        
        boss.setLocalTranslation((float) xLocation, 2f, (float) zLocation);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        boss.setMaterial((Material) assetManager.loadMaterial("Materials/Rock_1.j3m"));
        mat.setColor("Color", ColorRGBA.Brown);
        boss.setMaterial(mat);
        boss.scale(4);
        
        bossCollision = new BetterCharacterControl(2, 24, 4000);
        
        bossCollision.setViewDirection(sage.getNode().getLocalTranslation());
        
        boss.addControl(bossCollision);
        bulletAppState.getPhysicsSpace().add(bossCollision);
    }
    
    // Called once to make all of the enemies for the entire floor
    protected void makeEnemies()
    {
        double xLocation;
        double zLocation;
        int roomCount = 0;
        for (Node room : rooms)
        {
            if(roomCount > 1)
            {
                break;
            }
            for (int row = 2; row <= roomGridWidth; row++)
            {
                for (int col = 2; col <= roomGridHeight; col++)
                {
                    if (room.getUserData("EnvironmentObject " + row + "-" + col))
                    {
                        // do nothing b/c environment exists there
                    }
                    else
                    {
                        if(getRandom(enemyChance) == 0) // small chance to make an enemy in each location
                        {
                            xLocation = getRoomItemXLocation(row);
                            zLocation = getRoomItemZLocation(col);
                            int random = getRandom(3);
                            if(random == 0)
                            {
                                makePuppet(room, xLocation, zLocation, "EnemyJointless/EnemyJointless.j3o", "RockTexture.jpg");
                            }
                            else if(random == 1)
                            {
                                makeSpinningEnemy(room, xLocation, zLocation, "EnemyLogSpike/EnemyLogSpike.j3o", "RockTexture.jpg");
                            } 
                            else
                            {
                                makeWisp(room, xLocation, zLocation);
                            }
                        }
                        else
                        {
                            continue;
                        }
                    }
                }
            }
            roomCount++;
        }
    }
    
    protected void makePowerUp()
    {
        Node powerUpNode = new Node();
        Box box = new Box(1f,1f,1f);
        powerUpNode.setName("PowerUpNode");
        Spatial powerUpBox = new Geometry("PowerUp", box);
        powerUpBox.move(-8f, 6f, 3f);
        powerUpBox.setName("PowerUp");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        powerUpBox.setMaterial(mat);
        powerUpNode.attachChild(powerUpBox);
        
        RigidBodyControl powerUpCollision = new RigidBodyControl(0.1f);
        
        powerUpBox.addControl(powerUpCollision);
        
        bulletAppState.getPhysicsSpace().add(powerUpCollision);
        
        rootNode.attachChild(powerUpNode);
    }
    
    protected void makeDefenseUp()
    {
        Node defenseUpNode = new Node();
        Box box = new Box(1f,1f,1f);
        defenseUpNode.setName("DefenseUpNode");
        Spatial defenseUpBox = new Geometry("DefenseUp", box);
        defenseUpBox.move(-2f, 6f, 6f);
        defenseUpBox.setName("DefenseUp");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        defenseUpBox.setMaterial(mat);
        defenseUpNode.attachChild(defenseUpBox);
        
        RigidBodyControl powerUpCollision = new RigidBodyControl(0.1f);
        
        defenseUpBox.addControl(powerUpCollision);
        
        bulletAppState.getPhysicsSpace().add(powerUpCollision);
        
        rootNode.attachChild(defenseUpNode);
    }
    
    protected void makeWisdom()
    {
        if(wisdomNumber < 0)
        {
             wisdomNumber = getWisdomRandom(4);
        }
        
        Node wisdomNode = new Node();
        Box box = new Box(1f,1f,1f);
        wisdomNode.setName("WisdomNode");
        Spatial wisdomBox = new Geometry("Wisdom", box);
        wisdomBox.move(-2f, 6f, 6f);
        wisdomBox.setName("Wisdom");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        wisdomBox.setMaterial(mat);
        wisdomNode.attachChild(wisdomBox);
        
        RigidBodyControl wisdomCollision = new RigidBodyControl(0.1f);
        
        wisdomBox.addControl(wisdomCollision);
        
        bulletAppState.getPhysicsSpace().add(wisdomCollision);
        
        rootNode.attachChild(wisdomNode);
    }
    
    protected void rockAttack()
    {
        Node attackNode = new Node();
        attackNode.setName("attackNode");
        Spatial attackBox = assetManager.loadModel("Models/RockSpike/RockSpike.j3o");
        attackBox.setLocalTranslation(sage.getNode().getLocalTranslation().x + (float)getNumRooms(-16, 16), 70f, sage.getNode().getLocalTranslation().z + (float)getNumRooms(-16, 16));
        attackBox.setName("Attack");
        attackBox.setMaterial((Material) assetManager.loadMaterial("Materials/Rock_1.j3m"));
        attackNode.attachChild(attackBox);
        
        RigidBodyControl attackCollision = new RigidBodyControl(8.0f);
        attackBox.addControl(attackCollision);
        
        bulletAppState.getPhysicsSpace().add(attackCollision);
        
        sage.getNode().attachChild(attackNode);
    }
    
    protected void energyAttack()
    {
        float xOffset = 0;
        float zOffset = 0;
        Node attackNode = new Node();
        Sphere attackSphere = new Sphere(32,32,2f);
        Spatial attackBox = new Geometry("Enemy", attackSphere);
        attackNode.setName("attackNode");
        if(sage.getFacing().equals("left"))
        {
            xOffset = -1f;
        }
        else if(sage.getFacing().equals("up"))
        {
            zOffset = -1f;
        }
        else if(sage.getFacing().equals("down"))
        {
            zOffset = 1f;
        }
        else if(sage.getFacing().equals("right"))
        {
            xOffset = 1f;
        }
        attackBox.setLocalTranslation(sage.getNode().getLocalTranslation().x + xOffset, 6f, sage.getNode().getLocalTranslation().z + zOffset);
        attackBox.setName("Attack");
        attackBox.setUserData("energy", true);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(.7f,.8f,.5f,.5f));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        attackBox.setMaterial(mat);
        attackNode.attachChild(attackBox);
        
        RigidBodyControl attackCollision = new RigidBodyControl(8.0f);
        attackBox.addControl(attackCollision);
        
        bulletAppState.getPhysicsSpace().add(attackCollision);
        
        sage.getNode().attachChild(attackNode);
    }
    
    protected void bossAttack()
    {
        if(shottimer == 500)
        {
            int bossHealth = rootNode.getChild("Boss").getUserData("health");
            if (bossHealth > 0)
            {
                shottimer = 0;
                makeBoss(rootNode, 0, 0, "EnemyBossHead/EnemyBossHead.j3o", "RockTexture.jpg");
            }
            else
            {
                rootNode.getChild("Boss").removeControl(rootNode.getChild("Boss").getControl(0));
                rootNode.getChild("Boss").removeFromParent();
            }
        }
        shottimer++;
    }
    
    protected void updateHUD()
    {
        guiNode.detachAllChildren();
        
        // Larger than the health indicator within the healthbar
        Geometry healthbarOutline = new Geometry("HealthbarOutline", new Quad(406f, 26f));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        healthbarOutline.setMaterial(mat);
        healthbarOutline.setLocalTranslation(20, 20, 0);
        
        guiNode.attachChild(healthbarOutline);
        
        Geometry healthbarBackground = new Geometry("HealthbarBackground", new Quad(400f, 20f));
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Black);
        healthbarBackground.setMaterial(mat2);
        healthbarBackground.setLocalTranslation(25, 25, 0);
       
        guiNode.attachChild(healthbarBackground);
        
        Geometry healthbar = new Geometry("Healthbar", new Quad(sage.getHealth() * 4, 20f));
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", ColorRGBA.Red);
        healthbar.setMaterial(mat3);
        healthbar.setLocalTranslation(25, 25, 0);
        
        guiNode.attachChild(healthbar);
        
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText numPotions = new BitmapText(guiFont, false);
        numPotions.setColor(ColorRGBA.White);
        numPotions.setSize(24);
        numPotions.setText(Integer.toString(sage.getPotions()));
        numPotions.setLocalTranslation(430f, 50f, 0);
        
        guiNode.attachChild(numPotions);
        
        Geometry potionIcon = new Geometry("PotionIcon", new Quad(30f, 30f));
        Material mat4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture potionTexture = assetManager.loadTexture("Textures/potionImage.png");
        mat4.setTexture("ColorMap", potionTexture);
        potionIcon.setMaterial(mat4);
        potionIcon.setLocalTranslation(450f, 20f, 0);
        
        guiNode.attachChild(potionIcon);
        
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText numPowerUp = new BitmapText(guiFont, false);
        numPowerUp.setColor(ColorRGBA.White);
        numPowerUp.setSize(24);
        numPowerUp.setText(Integer.toString(sage.getNumPowerUp()));
        numPowerUp.setLocalTranslation(490f, 50f, 0);
        
        guiNode.attachChild(numPowerUp);
        
        Geometry powerUpIcon = new Geometry("PowerUpIcon", new Quad(30f, 30f));
        Material mat5 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat5.setColor("Color", ColorRGBA.Blue);
        powerUpIcon.setMaterial(mat5);
        powerUpIcon.setLocalTranslation(510f, 20f, 0);
        
        guiNode.attachChild(powerUpIcon);
        
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText numDefenseUp = new BitmapText(guiFont, false);
        numDefenseUp.setColor(ColorRGBA.White);
        numDefenseUp.setSize(24);
        numDefenseUp.setText(Integer.toString(sage.getNumDefenseUp()));
        numDefenseUp.setLocalTranslation(550f, 50f, 0);
        
        guiNode.attachChild(numDefenseUp);
        
        Geometry defenseUpIcon = new Geometry("DefenseUpIcon", new Quad(30f, 30f));
        Material mat6 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat6.setColor("Color", ColorRGBA.Green);
        defenseUpIcon.setMaterial(mat6);
        defenseUpIcon.setLocalTranslation(570f, 20f, 0);
        
        guiNode.attachChild(defenseUpIcon);
    }
    
    // Method for all character status related updates - e.g. isAlive, isPoisoned, isAsleep (if we ever do anything like this
    protected void updateCharacterStatus()
    {
        if(sage.getHealth() <= 0)
        {
            endGameSound.playInstance();
            gameOver = true;
        }
        if(!gameOver && !atTitleScreen)
        {
            walkingDirection.set(0,0,0);
            puppetWalkingDirection.set(0,0,0); // Have these in here due to a lack of time
            
            if(sage.getMoveLeft())
            {
                walkingDirection.addLocal(-sage.getSpeed() * 20,0,0);
                puppetWalkingDirection.addLocal(sage.getSpeed() * 40,0,0);
            }
            if(sage.getMoveUp())
            {
                walkingDirection.addLocal(0,0,-sage.getSpeed() * 20);
                puppetWalkingDirection.addLocal(0,0,sage.getSpeed() * 40);
            }
            if(sage.getMoveRight())
            {
                walkingDirection.addLocal(sage.getSpeed() * 20,0,0);
                puppetWalkingDirection.addLocal(-sage.getSpeed() * 40,0,0);
            }
            if(sage.getMoveDown())
            {
                walkingDirection.addLocal(0,0,sage.getSpeed() * 20);
                puppetWalkingDirection.addLocal(0,0,-sage.getSpeed() * 40);
            }
            playerControl.setWalkDirection(walkingDirection);
        }
    }
    
    protected void updateEnemyStatus()
    {
        if(!gameOver && !atTitleScreen)
        {
            for(int i = 0; i < enemyCollision.length; i++)
            {
                if(enemyCollision[i] == null)
                {
                    break;
                }
                enemyCollision[i].setWalkDirection(puppetWalkingDirection);
                enemyCollision[i].setViewDirection(new Vector3f(rotation.getX(), rotation.getY(), rotation.getZ()));
            }
            for(int i = 0; i < randomEnemyCollision.length; i++) // random movement
            {
                if(randomEnemyCollision[i] == null)
                {
                    break;
                }
                
                int random = getRandom(4);
                
                if(enemyMovementIndex <= 0)
                {
                    randomWalkingDirection.set(0,0,0);
                    enemyMovementIndex++;
                    if(random <= 0)
                    {
                        randomWalkingDirection.addLocal(-30,0,0);
                    }
                    else if(random == 1)
                    {
                        randomWalkingDirection.addLocal(0,0,-30);
                    }
                    else if(random == 2)
                    {
                        randomWalkingDirection.addLocal(30,0,0);
                    }
                    else if(random == 3)
                    {
                        randomWalkingDirection.addLocal(0,0,30);
                    }
                }
                else if(enemyMovementIndex <= 50)
                {
                    enemyMovementIndex++;
                }
                else
                {
                    enemyMovementIndex = 0;
                    System.out.println("Resetting index (e)...");
                }
                
                if(enemyXRotation < 5 && enemyXRotation >=0)
                {
                    enemyXRotation++;
                }
                else if(enemyXRotation < 0 && enemyXRotation > -5)
                {
                    enemyXRotation--;
                }
                
                if(enemyZRotation < 5 && enemyZRotation >=0)
                {
                    enemyZRotation++;
                }
                else if(enemyZRotation < 0 && enemyZRotation > -5)
                {
                    enemyZRotation--;
                }
                
                randomEnemyCollision[i].setWalkDirection(randomWalkingDirection);
                //randomEnemyCollision[i].setViewDirection(new Vector3f(enemyXRotation, rotation.getY(), enemyZRotation));
            }
        }
    }
    
    // UNUSED METHOD
    protected void makePlayer()
    {
        sage.setNode(rootNode);
        sage.getNode().setLocalTranslation(rootNode.getLocalTranslation());

        Spatial playerBox = assetManager.loadModel("Models/Sacboy.j3o");
        playerBox.setLocalTranslation(0f, 7f, 0f);
        playerBox.setName("Player");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Magenta);
        playerBox.setMaterial(mat);
        sage.getNode().attachChild(playerBox);
        
        // Adding the player to the physical space (allowing for collision)
        playerCollision = new RigidBodyControl(0.1f);
        playerBox.addControl(playerCollision);
        playerBox.getControl(RigidBodyControl.class).setKinematic(false);
        bulletAppState.getPhysicsSpace().add(playerCollision);

        //playerRay = new Ray(player.getChild("Player").getLocalTranslation(), rootNode.getChild("Top Wall 2").getLocalTranslation());
        //playerRay.setLimit(.00001f);
        
        rootNode.attachChild(sage.getNode());
    }

    // Create a floor with multiple rooms
    protected void makeFloor()
    {
        // Change background color of the display
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

        int numRooms = getNumRooms(minRooms, maxRooms);
        System.out.println(numRooms);
        // Determining the number of rooms that will be on the current floor
        rooms = new Node[numRooms];

        // 2400 is an arbitrary large value to avoid indexing issues
        wallCollision = new RigidBodyControl[numRooms * 400];
        floorCollision = new RigidBodyControl[numRooms];
        environmentCollision = new RigidBodyControl[numRooms * 200];
        enemyCollision = new BetterCharacterControl[numRooms * 200];
        randomEnemyCollision = new BetterCharacterControl[numRooms * 200];
        
        initRooms();
        initNeighborData();
        initEnvironmentData();

        // Initially build the room around the root node
        makeGround(rootNode, 0);
        makeWalls(rootNode);

        // Arbitrary values - user data cannot be negative, so I start with a high number
        int row = 100;
        int col = 100;

        // Setting the row and col of the rootNode
        rootNode.setUserData("row", row);
        rootNode.setUserData("col", col);

        // Looping efficiency variables
        int lastRoll = -1;
        int breakCounter = 0;

        for (int i = 0; i < rooms.length; i++)
        {
            breakCounter++;

            // Selecting the type of the next room randomly
            int nextRoom = getRandom(4);

            // Restart if the same failed number was rolled again
            if (lastRoll == nextRoom)
            {
                i--;
                continue;
            }

            // Break if it may be caught in an endless loop
            if (breakCounter > 20)
            {
                break;
            }

            // Number values represent a direction
            // 0 -> left
            // 1 -> up
            // 2 -> right
            // 3 -> down

            if (nextRoom == 0)
            {
                col -= 1;

                // If the coordinates are not already taken - continue
                if (!checkRoomCoordinates(row, col))
                {
                    rooms[i].setLocalTranslation(-70.001f, 0f, 0f);
                    rooms[i].setUserData("row", row);
                    rooms[i].setUserData("col", col);
                    rooms[i].setUserData("rightNeighbor", true);
                    if (i > 0)
                    {
                        rooms[i - 1].setUserData("leftNeighbor", true);
                    } 
                    else
                    {
                        rootNode.setUserData("leftNeighbor", true);
                    }
                } else
                {
                    lastRoll = 0;
                    col += 1;
                    i--;
                    continue;
                }
            }
            if (nextRoom == 1)
            {
                row -= 1;

                if (!checkRoomCoordinates(row, col))
                {
                    rooms[i].setLocalTranslation(0f, 0f, -50.001f);
                    rooms[i].setUserData("row", row);
                    rooms[i].setUserData("col", col);
                    rooms[i].setUserData("bottomNeighbor", true);
                    if (i > 0)
                    {
                        rooms[i - 1].setUserData("topNeighbor", true);
                    } else
                    {
                        rootNode.setUserData("topNeighbor", true);
                    }

                } else
                {
                    lastRoll = 1;
                    row += 1;
                    i--;
                    continue;
                }
            }
            if (nextRoom == 2)
            {
                col += 1;

                if (!checkRoomCoordinates(row, col))
                {
                    rooms[i].setLocalTranslation(70.001f, 0f, 0f);
                    rooms[i].setUserData("row", row);
                    rooms[i].setUserData("col", col);
                    rooms[i].setUserData("leftNeighbor", true);
                    if (i > 0)
                    {
                        rooms[i - 1].setUserData("rightNeighbor", true);
                    } else
                    {
                        rootNode.setUserData("rightNeighbor", true);
                    }
                } else
                {
                    lastRoll = 2;
                    col -= 1;
                    i--;
                    continue;
                }
            }
            if (nextRoom == 3)
            {
                row += 1;

                if (!checkRoomCoordinates(row, col))
                {
                    rooms[i].setLocalTranslation(0f, 0f, 50.001f);
                    rooms[i].setUserData("row", row);
                    rooms[i].setUserData("col", col);
                    rooms[i].setUserData("topNeighbor", true);
                    if (i > 0)
                    {
                        rooms[i - 1].setUserData("bottomNeighbor", true);
                    } else
                    {
                        rootNode.setUserData("bottomNeighbor", true);
                    }
                } else
                {
                    lastRoll = 3;
                    row -= 1;
                    i--;
                    continue;
                }
            }

            // Must attach the child room to the rootNode during the first iteration
            if (i == 0)
            {
                rootNode.attachChild(rooms[i]);
            }
            else
            {
                rooms[i - 1].attachChild(rooms[i]);
            }

            makeGround(rooms[i], 0);
            makeWalls(rooms[i]);
            makeDoors();

            // Reset loop efficiency variables
            lastRoll = -1;
            breakCounter = 0;
        }
    }

    protected void initRooms()
    {
        rootNode.setUserData("isRendered", false);
        for (int i = 0; i < rooms.length; i++)
        {
            rooms[i] = new Node();
            rooms[i].setUserData("row", 0);
            rooms[i].setUserData("col", 0);
            rooms[i].setUserData("isRendered", false);
        }
    }

    // Creates the floor of a room
    protected void makeGround(Node room, int roomNum)
    {
        Box box = new Box(35, .2f, 25);
        Geometry floor = new Geometry("Floor", box);
        floor.setLocalTranslation(0f, 0f, 0f);
        TangentBinormalGenerator.generate(box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture floorTexture = assetManager.loadTexture("Textures/FloorBlue.jpg");
        mat.setTexture("ColorMap", floorTexture);
        floor.setMaterial(mat);
        room.attachChild(floor);
        floorCollision[roomNum] = new RigidBodyControl(0.0f);
        floor.addControl(floorCollision[roomNum]);
        floor.getControl(RigidBodyControl.class).setKinematic(true);
        bulletAppState.getPhysicsSpace().add(floorCollision[roomNum]);
    }

    // Creates the walls of a room
    protected void makeWalls(Node room)
    {
        // Left and right walls 
        //Box box = new Box(.2f, 10f, blockHeight);
        // Top and bottom walls
        //Box box2 = new Box(35f, 10f, .2f);

        Geometry[] leftWall = initWall("left", .5f, 10f, blockHeight);
        Geometry[] rightWall = initWall("right", .5f, 10f, blockHeight);
        Geometry[] topWall = initWall("top", blockWidth, 10f, .5f);
        Geometry[] bottomWall = initWall("bottom", blockWidth, 10f, .5f);

        // Attach the left and right walls to the node - could be a method?
        for (int i = 0; i < leftWall.length - 1; i++)
        {
            room.attachChild(leftWall[i]);
            room.attachChild(rightWall[i]);
            // Adding the wall to the physical space
            wallCollision[wallCollisionIndex] = new RigidBodyControl(0.0f);
            leftWall[i].addControl(wallCollision[wallCollisionIndex]);
            leftWall[i].getControl(RigidBodyControl.class).setKinematic(true);
            bulletAppState.getPhysicsSpace().add(wallCollision[wallCollisionIndex]);
            
            wallCollisionIndex++;

            wallCollision[wallCollisionIndex] = new RigidBodyControl(0.0f);
            rightWall[i].addControl(wallCollision[wallCollisionIndex]);
            rightWall[i].getControl(RigidBodyControl.class).setKinematic(true);
            bulletAppState.getPhysicsSpace().add(wallCollision[wallCollisionIndex]);
            
            wallCollisionIndex++;
        }

        // Attach the top and bottom walls to the node - could be a method?
        for (int i = 0; i < topWall.length - 1; i++)
        {
            room.attachChild(topWall[i]);
            room.attachChild(bottomWall[i]);
            // Adding the wall to the physical space
            wallCollision[wallCollisionIndex] = new RigidBodyControl(0.0f);
            topWall[i].addControl(wallCollision[wallCollisionIndex]);
            topWall[i].getControl(RigidBodyControl.class).setKinematic(true);
            bulletAppState.getPhysicsSpace().add(wallCollision[wallCollisionIndex]);

            wallCollisionIndex++;

            wallCollision[wallCollisionIndex] = new RigidBodyControl(0.0f);
            bottomWall[i].addControl(wallCollision[wallCollisionIndex]);
            bottomWall[i].getControl(RigidBodyControl.class).setKinematic(true);
            bulletAppState.getPhysicsSpace().add(wallCollision[wallCollisionIndex]);

            wallCollisionIndex++;
        }
    }

    protected Geometry[] initWall(String side, float wallX, float wallY, float wallZ)
    {
        Box box = new Box(wallX, wallY, wallZ);
        Geometry[] wall;
        int wallLocation;

        // Determine how many segments are necessary in the wall
        if (side.equals("top") || side.equals("bottom"))
        {
            wall = new Geometry[8];
            wallLocation = -30;
        } else
        {
            wall = new Geometry[6];
            wallLocation = -20;
        }

        //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //Texture wallTexture = assetManager.loadTexture("Textures/BrickGrey.jpg");
        //mat.setTexture("ColorMap", wallTexture);

        for (int i = 0; i < wall.length - 1; i++)
        {
            if (side.equals("left"))
            {
                wall[i] = new Geometry("Left Wall " + i, box);
                wall[i].setLocalTranslation(-35f, 5f, wallLocation);
            }
            if (side.equals("top"))
            {
                wall[i] = new Geometry("Top Wall " + i, box);
                wall[i].setLocalTranslation(wallLocation, 5f, -25f);
            }
            if (side.equals("right"))
            {
                wall[i] = new Geometry("Right Wall " + i, box);
                wall[i].setLocalTranslation(35f, 5f, wallLocation);
            }
            if (side.equals("bottom"))
            {
                wall[i] = new Geometry("Bottom Wall " + i, box);
                wall[i].setLocalTranslation(wallLocation, 5f, 25f);
            }

            //wall[i].setMaterial(mat);
            wall[i].setMaterial((Material) assetManager.loadMaterial("Materials/GreyBrick.j3m"));

            wallLocation += blockHeight * 2;
        }

        return wall;
    }

    protected void initNeighborData()
    {
        rootNode.setUserData("leftNeighbor", false);
        rootNode.setUserData("topNeighbor", false);
        rootNode.setUserData("rightNeighbor", false);
        rootNode.setUserData("bottomNeighbor", false);

        for (Node room : rooms)
        {
            room.setUserData("leftNeighbor", false);
            room.setUserData("topNeighbor", false);
            room.setUserData("rightNeighbor", false);
            room.setUserData("bottomNeighbor", false);
        }
    }

    // Initializing the environment user data for each room node
    protected void initEnvironmentData()
    {
        int random = -1;

        for (int row = 1; row <= roomGridWidth; row++)
        {
            for (int col = 1; col <= roomGridHeight; col++)
            {
                // Gives a 20% chance for an object to be placed in each location
                random = getRandom(20);

                if (random == 1) // 1 is an arbitrary 'magic number'
                {
                    rootNode.setUserData("EnvironmentObject " + row + "-" + col, true);
                } else
                {
                    rootNode.setUserData("EnvironmentObject " + row + "-" + col, false);
                }
            }
        }

        for (Node room : rooms)
        {
            for (int row = 2; row <= roomGridWidth; row++)
            {
                for (int col = 2; col <= roomGridHeight; col++)
                {
                    random = getRandom(20);

                    if (random == 1) // 1 is an arbitrary 'magic number'
                    {
                        room.setUserData("EnvironmentObject " + row + "-" + col, true);
                    } else
                    {
                        room.setUserData("EnvironmentObject " + row + "-" + col, false);
                    }
                }
            }
        }
    }

    // Called to set user data to determine where environment pieces should go
    protected void makeEnvironment()
    {
        // Displacement variables
        double xLocation = 0;
        double zLocation = 0;

        for (int row = 2; row <= roomGridWidth; row++)
        {
            for (int col = 2; col <= roomGridHeight; col++)
            {
                if (rootNode.getUserData("EnvironmentObject " + row + "-" + col))
                {
                    xLocation = getRoomItemXLocation(row);
                    zLocation = getRoomItemZLocation(col);
                    makeEnvironmentItem(rootNode, xLocation, zLocation, "Rock2/Rock2.j3o", "RockTexture.jpg");
                }
            }
        }

        for (Node room : rooms)
        {
            for (int row = 2; row <= roomGridWidth; row++)
            {
                for (int col = 2; col <= roomGridHeight; col++)
                {
                    if (room.getUserData("EnvironmentObject " + row + "-" + col))
                    {
                        xLocation = getRoomItemXLocation(row);
                        zLocation = getRoomItemZLocation(col);
                        makeEnvironmentItem(room, xLocation, zLocation, "Rock2/Rock2.j3o", "RockTexture.jpg");
                    }
                }
            }
        }

    }

    // NEEDS TO BE FINISHED - IGNORE THIS - will be used for random door placement
    protected void addNeighborData()
    {
        int row;
        int col;
        int nextRow;
        int nextCol;

        for (int i = 0; i < rooms.length; i++)
        {
            row = rooms[i].getUserData("row");
            col = rooms[i].getUserData("col");
        }
    }

    // Returns the xLocation of an object to be placed in a room
    protected double getRoomItemXLocation(int row)
    {
        // Simple displacement algorithm
        double xLocation = (row - 8) * 5;

        return xLocation;
    }

    // Returns the zLocation of an object to be placed in a room
    protected double getRoomItemZLocation(int col)
    {
        // Simple displacement algorithm
        double zLocation = (col - 6) * 5;

        return zLocation;
    }

    protected void makeEnvironmentItem(Node room, double xLocation, double zLocation, String model, String texture)
    {
        Spatial environmentItem = assetManager.loadModel("Models/" + model);
        environmentItem.setLocalTranslation((float) xLocation, 2f, (float) zLocation);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //mat.setColor("Color", ColorRGBA.Brown);
        
        environmentItem.setMaterial((Material) assetManager.loadMaterial("Materials/Rock_1.j3m"));
        
         environmentCollision[environmentCollisionIndex] = new RigidBodyControl(0.0f);
         environmentItem.addControl(environmentCollision[environmentCollisionIndex]);
         environmentItem.getControl(RigidBodyControl.class).setKinematic(true);
         bulletAppState.getPhysicsSpace().add(environmentCollision[environmentCollisionIndex]);
      
         environmentCollisionIndex++;

        room.attachChild(environmentItem);
    }

    // Detaches wall segments as needed
    protected void makeDoors()
    {
        if (rootNode.getUserData("topNeighbor"))
        {
            //rootNode.detachChildNamed("Top Wall 3");
            rootNode.getChild("Top Wall 3").setLocalTranslation(new Vector3f(0, -1000f, 0));
        }
        if (rootNode.getUserData("bottomNeighbor"))
        {
            //rootNode.detachChildNamed("Bottom Wall 3");
            rootNode.getChild("Bottom Wall 3").setLocalTranslation(new Vector3f(0, -1000f, 0));
        }
        if (rootNode.getUserData("leftNeighbor"))
        {
            //rootNode.detachChildNamed("Left Wall 2");
            rootNode.getChild("Left Wall 2").setLocalTranslation(new Vector3f(0, -1000f, 0));
        }
        if (rootNode.getUserData("rightNeighbor"))
        {
            //rootNode.detachChildNamed("Right Wall 2");
            rootNode.getChild("Right Wall 2").setLocalTranslation(new Vector3f(0, -1000f, 0));
        }

        for (int i = 0; i < rooms.length; i++)
        {
            if (rooms[i].getUserData("topNeighbor"))
            {
                //rooms[i].detachChildNamed("Top Wall 3");
                rooms[i].getChild("Top Wall 3").setLocalTranslation(new Vector3f(0, -1000f, 0));
            }
            if (rooms[i].getUserData("bottomNeighbor"))
            {
                //rooms[i].detachChildNamed("Bottom Wall 3");
                rooms[i].getChild("Bottom Wall 3").setLocalTranslation(new Vector3f(0, -1000f, 0));
            }
            if (rooms[i].getUserData("leftNeighbor"))
            {
                //rooms[i].detachChildNamed("Left Wall 2");
                rooms[i].getChild("Left Wall 2").setLocalTranslation(new Vector3f(0, -1000f, 0));
            }
            if (rooms[i].getUserData("rightNeighbor"))
            {
                //rooms[i].detachChildNamed("Right Wall 2");
                rooms[i].getChild("Right Wall 2").setLocalTranslation(new Vector3f(0, -1000f, 0));
            }
        }
    }

    // Sets the camera to the desired location
    protected void setupCamera(Node room, float x, float y, float z)
    {
        flyCam.setEnabled(false);
        //cam.setFrustumPerspective(90, settings.getWidth()/settings.getHeight(), 1, 1000);
        camNode = new CameraNode("Camera Node", cam);
        camera.setCameraNode((CameraNode) camNode);
        room.attachChild(camera.getCameraNode());
        camera.getCameraNode().setLocalTranslation(new Vector3f(x, y, z));
        camNode.lookAt(sage.getNode().getLocalTranslation(), Vector3f.UNIT_Y); // Change room to rootNode as needed for testing
    }

    // Initializes ambient light and creates a light to make the rooms brighter
    protected void initLight()
    {
        ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(ambient);
        roomLight = new PointLight();
        roomLight.setPosition(camera.getNode().getLocalTranslation());
        rootNode.addLight(roomLight);
        LightControl lightControl = new LightControl();
        camera.getCameraNode().addControl(lightControl);
    }

    // Makes a directional light in any room as needed
    protected void makeLight(Node room)
    {
        sunlight = new DirectionalLight();
        sunlight.setColor(ColorRGBA.White);
        sunlight.setDirection(room.getLocalTranslation().normalizeLocal());
        room.addLight(sunlight);
    }

    // Returns a random number below the max value
    protected int getRandom(int max)
    {
        return randomGenerator.nextInt(max);
    }

    protected int getWisdomRandom(int max)
    {
        int wisdom = randomGenerator.nextInt(max);
        
        if(wisdomUnlock[wisdom])
        {
            getWisdomRandom(max);
        }
        
        return wisdom;
    }
    
    // Returns a random number between two values
    protected int getNumRooms(int min, int max)
    {
        return randomGenerator.nextInt(max - min) + min;
    }

    // Checks if the coordinates for the next room are already taken
    protected boolean checkRoomCoordinates(int nextRow, int nextCol)
    {
        int row;
        int col;
        int rootRow = rootNode.getUserData("row");
        int rootCol = rootNode.getUserData("col");

        for (Node room : rooms)
        {
            row = room.getUserData("row");
            col = room.getUserData("col");
            if (row == nextRow && col == nextCol)
            {
                return true;
            } 
            else if (nextRow == rootRow && nextCol == rootCol)
            {
                return true;
            }
        }
        return false;
    }
   
    // Renders only the rooms closest to the player - needs work
    protected void renderNearestRooms()
    {
        double renderDistance = 200;
        boolean isRendered = rootNode.getUserData("isRendered");


        if ((player.getChild("Player").getLocalTranslation().distance(rootNode.getLocalTranslation()) < renderDistance) && !isRendered)
        {
            makeGround(rootNode, 0);
            makeWalls(rootNode);
            makeDoors();
            //makeEnvironment(rootNode);
            rootNode.setUserData("isRendered", true);
        }

        for (int i = 0; i < rooms.length - 1; i++)
        {
            double roomDistance = player.getChild("Player").getLocalTranslation().distance(rooms[i].getLocalTranslation());
            isRendered = rooms[i].getUserData("isRendered");
            if (roomDistance < renderDistance && !isRendered)
            {
                makeGround(rooms[i], 0);
                makeWalls(rooms[i]);
                makeDoors();
                //makeEnvironment(rooms[i]);

                rooms[i].setUserData("isRendered", true);
            } else if (roomDistance > renderDistance)
            {
                for (int j = 0; j < 6; j++)
                {
                    rooms[i].detachChildNamed("Left Wall " + j);
                    rooms[i].detachChildNamed("Right Wall " + j);
                }
                for (int j = 0; j < 8; j++)
                {
                    rooms[i].detachChildNamed("Top Wall " + j);
                    rooms[i].detachChildNamed("Bottom Wall " + j);
                }
                rooms[i].detachChildNamed("Floor");
                rooms[i].detachChildNamed("EnvironmentObject");
                rooms[i].setUserData("isRendered", false);
            }
        }
    }

    /* protected void checkRoomCollision(Ray ray)
     {
     // Add in collision for rootNode and final room node rooms[length - 1]
     for(int i = 0; i < rooms.length - 1; i++)
     {
     for(int j = 0; j < 7; j++)
     {
     try
     {
     rootNode.getChild("Top Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[i].getChild("Top Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[rooms.length - 1].getChild("Top Wall " + Integer.toString(j)).collideWith(ray, results);
     }
     catch(NullPointerException e)
     {
     continue;
     }
          
     }
     }
     if (results.size() > 0) 
     {
     sage.setAllowUp(false);
     }
     else
     {
     sage.setAllowUp(false);
     }
      
     results.clear();
      
     for(int i = 0; i < rooms.length - 1; i++)
     {
     for(int j = 0; j < 5; j++)
     {
     try
     {
     rootNode.getChild("Left Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[i].getChild("Left Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[rooms.length - 1].getChild("Left Wall " + Integer.toString(j)).collideWith(ray, results);
     }
     catch(NullPointerException e)
     {
     continue;
     }
          
     }
     }
     if (results.size() > 0) 
     {
     sage.setAllowLeft(false);
     }
     else
     {
     sage.setAllowLeft(true);
     }
      
     results.clear();
      
     for(int i = 0; i < rooms.length - 1; i++)
     {
     for(int j = 0; j < 5; j++)
     {
     try
     {
     rootNode.getChild("Right Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[i].getChild("Right Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[rooms.length - 1].getChild("Right Wall " + Integer.toString(j)).collideWith(ray, results);
     }
     catch(NullPointerException e)
     {
     continue;
     }
          
     }
     }
      
     if (results.size() > 0) 
     {
     sage.setAllowRight(false);
     }
     else
     {
     sage.setAllowRight(false);
     }
      
     results.clear();
      
     for(int i = 0; i < rooms.length - 1; i++)
     {
     for(int j = 0; j < 7; j++)
     {
     try
     {
     rootNode.getChild("Bottom Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[i].getChild("Bottom Wall " + Integer.toString(j)).collideWith(ray, results);
     rooms[rooms.length - 1].getChild("Bottom Wall " + Integer.toString(j)).collideWith(ray, results);
     }
     catch(NullPointerException e)
     {
     continue;
     }
          
     }
     }
      
      
     if (results.size() > 0) 
     {
     sage.setAllowDown(false);
     }
     else
     {
     sage.setAllowDown(true);
     }
      
     results.clear();
     */
    
    private void initSounds()
    {
        // Running sound does not play when you reset the game
        runningSound = new AudioNode(assetManager, "Sounds/WalkingSound.wav", true);
        runningSound.setPositional(false);
        runningSound.setLooping(false);
        runningSound.setVolume(2);
        rootNode.attachChild(runningSound);
        
        healingSound = new AudioNode(assetManager, "Sounds/HealSound.wav", false);
        healingSound.setPositional(false);
        healingSound.setLooping(false);
        healingSound.setVolume(2000);
        rootNode.attachChild(healingSound);
        
        startGameSound = new AudioNode(assetManager, "Sounds/DoorOpening.wav", false);
        startGameSound.setPositional(false);
        startGameSound.setLooping(false);
        startGameSound.setVolume(2000);
        rootNode.attachChild(startGameSound);
        
        endGameSound = new AudioNode(assetManager, "Sounds/DoorClosing.wav", false);
        endGameSound.setPositional(false);
        endGameSound.setLooping(false);
        endGameSound.setVolume(2);
        rootNode.attachChild(endGameSound);
        
        useItemSound = new AudioNode(assetManager, "Sounds/Pickup.wav", false);
        useItemSound.setPositional(false);
        useItemSound.setLooping(false);
        useItemSound.setVolume(2);
        rootNode.attachChild(useItemSound);
        
        windSound = new AudioNode(assetManager, "Sounds/Wind.wav", false);
        windSound.setPositional(false);
        windSound.setLooping(false);
        windSound.setVolume(2);
        rootNode.attachChild(windSound);
    }
    
    // Initializes key bindings - we will use booleans to create game states
    private void initKeys()
    {
        inputManager.addMapping("StartGame", new KeyTrigger(KeyInput.KEY_SPACE));
        
        inputManager.addMapping("Zoom", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping("CameraLeft", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("CameraUp", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("CameraRight", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("CameraDown", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("CameraReset", new KeyTrigger(KeyInput.KEY_X));

        inputManager.addMapping("PlayerLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("PlayerUp", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("PlayerRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("PlayerDown", new KeyTrigger(KeyInput.KEY_S));
        
        inputManager.addMapping("IncreaseSpeed", new KeyTrigger(KeyInput.KEY_0));
        inputManager.addMapping("DecreaseSpeed", new KeyTrigger(KeyInput.KEY_9));
        
        inputManager.addMapping("IncreaseHealth", new KeyTrigger(KeyInput.KEY_8));
        inputManager.addMapping("DecreaseHealth", new KeyTrigger(KeyInput.KEY_7));
        
        inputManager.addMapping("IncreaseEnemies", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("DecreaseEnemies", new KeyTrigger(KeyInput.KEY_1));
        
        inputManager.addMapping("UsePotion", new KeyTrigger(KeyInput.KEY_B));
        inputManager.addMapping("UsePowerUp", new KeyTrigger(KeyInput.KEY_N));
        inputManager.addMapping("UseDefenseUp", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("OpenMenu", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addMapping("OpenMerchant", new KeyTrigger(KeyInput.KEY_L));

        inputManager.addMapping("PlayerAttack", new KeyTrigger(KeyInput.KEY_RCONTROL));
        inputManager.addMapping("RockAttack", new KeyTrigger(KeyInput.KEY_RSHIFT));
        
        inputManager.addMapping("UnlockEnemies", new KeyTrigger(KeyInput.KEY_LCONTROL));

        inputManager.addListener(actionListener, "UnlockEnemies", "Zoom", "CameraReset", "StartGame", "UsePotion", "PlayerLeft", "PlayerUp", "PlayerRight", "PlayerDown", "OpenMenu", "UsePowerUp", "UseDefenseUp", "OpenMerchant", "PlayerAttack", "RockAttack");
        inputManager.addListener(analogListener, "CameraLeft", "CameraUp", "CameraRight", "CameraDown", "PlayerLeft", "PlayerUp", "PlayerRight", "PlayerDown", "IncreaseSpeed", "DecreaseSpeed", "IncreaseHealth", "DecreaseHealth", "IncreaseEnemies", "DecreaseEnemies");
    }
    // Action listener is for actions that should only happen once in a given moment
    private ActionListener actionListener = new ActionListener()
    {
        public void onAction(String name, boolean keyPressed, float tpf)
        {
            if(!gameOver)
            {
                if (name.equals("Zoom") && !keyPressed)
                {
                    if (camera.getY() == 65)
                    {
                        camera.setY(750);
                        camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
                    } 
                    else
                    {
                        camera.setY(65);
                        camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
                    }
                }
                if (name.equals("CameraReset") && !keyPressed)
                {
                    camera.setX(0);
                    camera.setZ(35);
                    camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
                }
                if(atTitleScreen)
                {
                    // when at the title screen and space is pressed, start the game
                    if (name.equals("StartGame") && !keyPressed)
                    {
                        startGame();
                        startGameSound.playInstance();
                        atTitleScreen = false;
                    }
                }
                if(merchantOpen)
                {
                    if(name.equals("StartGame") && !keyPressed)
                    {
                        if(textIndex < 5 && !metMerchant)
                        {
                            textIndex++;
                        }
                        else if(!metMerchant)
                        {
                            metMerchant = true;
                        }
                        else if(gaveMerchant)
                        {
                            gaveMerchant = false;
                        }
                    }
                    if(name.equals("UsePowerUp") && !keyPressed && sage.getNumPowerUp() > 0 && metMerchant)
                    {
                        sage.setNumPowerUp(-1);
                        sage.setPotions(2);
                        useItemSound.playInstance();
                        gaveMerchant = true;
                        initSounds();
                    }
                    if(name.equals("UseDefenseUp") && !keyPressed && sage.getNumDefenseUp() > 0 && metMerchant)
                    {
                        sage.setNumDefenseUp(-1);
                        sage.setPotions(2);
                        useItemSound.playInstance();
                        gaveMerchant = true;
                        initSounds();
                    }
                }
                if (name.equals("UsePotion") && !keyPressed && sage.getPotions() > 0)
                {
                    // Removing a potion and fully healing the player
                    sage.setPotions(-1);
                    sage.setHealth(100 - sage.getHealth());
                    healingSound.playInstance();
                    initSounds(); //should come out later - temporary fix
                }
                if (name.equals("UsePowerUp") && !keyPressed && sage.getNumPowerUp() > 0 && !merchantOpen)
                {
                    // Removing a potion and fully healing the player
                    sage.setNumPowerUp(-1);
                    sage.setAttack(1);
                    useItemSound.playInstance();
                    initSounds(); //should come out later - temporary fix
                }
                if (name.equals("UseDefenseUp") && !keyPressed && sage.getNumDefenseUp() > 0 && !merchantOpen)
                {
                    // Removing a potion and fully healing the player
                    sage.setNumDefenseUp(-1);
                    sage.setDefense(1);
                    useItemSound.playInstance();
                    initSounds(); //should come out later - temporary fix
                }
                if (name.equals("PlayerLeft"))
                {
                    sage.setMoveLeft(keyPressed);
                    if(!animationChannel.getAnimationName().equals("Walk"))
                    {
                        animationChannel.setAnim("Walk", 0.50f);
                        animationChannel.setLoopMode(LoopMode.Loop);
                        animationChannel.setSpeed(1.5f);
                    }
                    if(!keyPressed) // On key release...do this
                    {
                        animationChannel.setAnim("stand", 0.50f);
                        animationChannel.setLoopMode(LoopMode.DontLoop);
                    }
                    
                }
                if (name.equals("PlayerUp"))
                {
                    sage.setMoveUp(keyPressed);
                    if(!animationChannel.getAnimationName().equals("Walk"))
                    {
                        animationChannel.setAnim("Walk", 0.50f);
                        animationChannel.setLoopMode(LoopMode.Loop);
                        animationChannel.setSpeed(1.5f);
                    }
                    if(!keyPressed) // On key release...do this
                    {
                        animationChannel.setAnim("stand", 0.50f);
                        animationChannel.setLoopMode(LoopMode.DontLoop);
                    }
                }
                if (name.equals("PlayerRight"))
                {
                    sage.setMoveRight(keyPressed);
                    if(!animationChannel.getAnimationName().equals("Walk"))
                    {
                        animationChannel.setAnim("Walk", 0.50f);
                        animationChannel.setLoopMode(LoopMode.Loop);
                        animationChannel.setSpeed(1.5f);
                    }
                    if(!keyPressed) // On key release...do this
                    {
                        animationChannel.setAnim("stand", 0.50f);
                        animationChannel.setLoopMode(LoopMode.DontLoop);
                    }
                }
                if (name.equals("PlayerDown"))
                {
                    sage.setMoveDown(keyPressed);
                    if(!animationChannel.getAnimationName().equals("Walk"))
                    {
                        animationChannel.setAnim("Walk", 0.50f);
                        animationChannel.setLoopMode(LoopMode.Loop);
                        animationChannel.setSpeed(1.5f);
                    }
                    if(!keyPressed) // On key release...do this
                    {
                        animationChannel.setAnim("stand", 0.50f);
                        animationChannel.setLoopMode(LoopMode.DontLoop);
                    }
                }
                if (name.equals("OpenMenu") && !keyPressed)
                {
                    if(wisdomOpen)
                    {
                        sage.setFoundWisdom(false);
                        wisdomOpen = false;
                        wisdomUnlock[wisdomNumber] = true;
                        wisdomNumber = -1;
                        
                    }
                    if(sage.getFoundWisdom())
                    {
                        wisdomOpen = true;
                    }
                    else if(!menuOpen)
                    {
                        menuOpen = true;
                    }
                    else
                    {
                        menuOpen = false;
                        merchantOpen = false;
                    }
                }
                if(name.equals("OpenMerchant") && !keyPressed)
                {
                    if(menuOpen)
                    {
                        if(merchantOpen)
                        {
                            merchantOpen = false;
                        }
                        else
                        {
                            merchantOpen = true;
                            windSound.playInstance();
                            initSounds();
                        }
                    }
                }
                if(name.equals("PlayerAttack") && !keyPressed)
                {
                    energyAttack();
                }
                if(name.equals("RockAttack") && !keyPressed)
                {
                    rockAttack();
                }
                if(name.equals("UnlockEnemies") && !keyPressed)
                {
                    unlockEnemies = true;
                }
            }
            else if(gameOver) // yes, it is redundant - just for readability
            {
                if (name.equals("StartGame") && !keyPressed)
                {
                    resetGame();
                    // Enabling the title screen and disabling the game over screen
                    atTitleScreen = true;
                    menuOpen = false;
                    gameOver = false;
                }
            }
            
        }
    };
    // Analog listener is for consistent actions that should be able to repeat constantly
    private AnalogListener analogListener = new AnalogListener()
    {
        public void onAnalog(String name, float value, float tpf)
        {
            if(!gameOver)
            {
                if (name.equals("CameraLeft"))
                {
                    camera.setX(-5f);
                    camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
                }
                if (name.equals("CameraUp"))
                {
                    camera.setZ(-5f);
                    camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
                }
                if (name.equals("CameraRight"))
                {
                    camera.setX(5f);
                    camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
                }
                if (name.equals("CameraDown"))
                {
                    camera.setZ(5f);
                    camera.setLocation(camera.getX(), camera.getY(), camera.getZ());
                }
                if (name.equals("PlayerLeft") && sage.getAllowLeft())
                {
                    sage.setFacing("left");
                    
                    sage.setX(-sage.getSpeed());
                
                    // Setting the character facing rotation angle
                    rotation.fromAngleAxis(FastMath.PI * 3 / 2, new Vector3f(0,1,0));
                    sage.getNode().getChild("Player").setLocalRotation(rotation);
                
                    //playerControl.warp(new Vector3f(sage.getX(), sage.getY(), sage.getZ()));
                    //playerControl.setWalkDirection(new Vector3f(-sage.getSpeed() * 3,0,0));
                    
                    //sage.getNode().getChild("Player").setLocalTranslation(sage.getX(), sage.getY(), sage.getZ());
                                        
                    //camera.setX(-sage.getSpeed() / 8);
                    camera.setLocation(sage.getNode().getLocalTranslation().x, camera.getY(), sage.getNode().getLocalTranslation().z);
                    
                    runningSound.play();
                }
                if (name.equals("PlayerUp") && sage.getAllowUp())
                {
                    sage.setFacing("up");
                    
                    sage.setZ(-sage.getSpeed());
                
                    // Setting the character facing rotation angle
                    rotation.fromAngleAxis(FastMath.PI, new Vector3f(0,1,0));
                    sage.getNode().getChild("Player").setLocalRotation(rotation);
                
                    //playerControl.warp(new Vector3f(sage.getX(), sage.getY(), sage.getZ()));
                    //playerControl.setWalkDirection(new Vector3f(0,0,-sage.getSpeed() * 3));
                            
                    //sage.getNode().getChild("Player").setLocalTranslation(sage.getX(), sage.getY(), sage.getZ());
                
                    //camera.setZ(-sage.getSpeed() / 8);
                    camera.setLocation(sage.getNode().getLocalTranslation().x, camera.getY(), sage.getNode().getLocalTranslation().z);
                    
                    runningSound.play();
                }
                if (name.equals("PlayerRight") && sage.getAllowRight())
                {
                    sage.setFacing("right");
                    
                    sage.setX(sage.getSpeed());
                
                    // Setting the character facing rotation angle
                    rotation.fromAngleAxis(FastMath.PI/2, new Vector3f(0,1,0));
                    sage.getNode().getChild("Player").setLocalRotation(rotation);
                    
                    //playerControl.warp(new Vector3f(sage.getX(), sage.getY(), sage.getZ()));
                    //playerControl.setWalkDirection(new Vector3f(sage.getSpeed() * 3,0,0));
                    //sage.getNode().getChild("Player").setLocalTranslation(sage.getX(), sage.getY(), sage.getZ());
                
                    //camera.setX(sage.getSpeed() / 8);
                    camera.setLocation(sage.getNode().getLocalTranslation().x, camera.getY(), sage.getNode().getLocalTranslation().z);
                    
                    runningSound.play();
                }
                if (name.equals("PlayerDown") && sage.getAllowDown())
                {
                    sage.setFacing("down");
                    
                    sage.setZ(sage.getSpeed());
                
                    //sage.getNode().getChild("Player").setLocalTranslation(sage.getX(), sage.getY(), sage.getZ());
                
                    // Setting the character facing rotation angle
                    rotation.fromAngleAxis(FastMath.PI * 2, new Vector3f(0,1,0));
                    sage.getNode().getChild("Player").setLocalRotation(rotation);
                
                    //playerControl.warp(new Vector3f(sage.getX(), sage.getY(), sage.getZ()));
                    //playerControl.setWalkDirection(new Vector3f(0,0,sage.getSpeed() * 3));
                     //sage.getNode().getChild("Player").setLocalTranslation(sage.getX(), sage.getY(), sage.getZ());
                
                    //camera.setZ(sage.getSpeed() / 8);
                    camera.setLocation(sage.getNode().getLocalTranslation().x, camera.getY(), sage.getNode().getLocalTranslation().z);
                    
                    runningSound.play();
                }
                if (name.equals("IncreaseSpeed"))
                {
                    if(sage.getSpeed() < 1f)
                    {
                        sage.setSpeed(.1f);
                    }
                }
                if (name.equals("DecreaseSpeed"))
                {
                    if(sage.getSpeed() > .1f)
                    {
                        sage.setSpeed(-.1f);
                    }
                }
                if (name.equals("IncreaseHealth"))
                {
                    if(sage.getHealth() < 100f)
                    {
                        sage.setHealth(1f);
                    }
                }
                if (name.equals("DecreaseHealth"))
                {
                    if(sage.getHealth() > 0f)
                    {
                        sage.setHealth(-1f);
                    }
                }
                if (name.equals("IncreaseEnemies"))
                {
                    if(enemyChance > 1)
                    {
                        enemyChance--;
                    }
                }
                if (name.equals("DecreaseEnemies"))
                {
                    if(enemyChance < 100)
                    {
                        enemyChance++;
                    }
                }
            }
        }
    };
    
    protected float getScreenWidth()
    {
        return this.settings.getWidth();
    }
    
    protected float getScreenHeight()
    {
        return this.settings.getHeight();
    }
    
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName)
    {
        
    }
    
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName)
    {
        
    }
    
    @Override
     public void simpleUpdate(float tpf) 
     {
         if(!gameOver && !atTitleScreen)
         {
            if(bossTest)
            {
                bossAttack();
            }
            updateHUD();
            updateCharacterStatus();
            updateEnemyStatus();
            if(sage.getHitEnemy())
            {
                sage.setHitEnemy(false);
            }
            if(wisdomOpen)
            {
                displayWisdom();
            }
            if(menuOpen)
            {
                if(merchantOpen)
                {
                    displayMerchant();
                }
                else
                {
                    displayMenu();
                }
            }
         }
         if(gameOver)
         {
             displayGameOver();
         }
         if(atTitleScreen)
         {
             displayTitleScreen();
         }
     }
}
