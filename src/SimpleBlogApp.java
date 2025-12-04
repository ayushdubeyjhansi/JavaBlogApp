import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class SimpleBlogApp extends JFrame {

    // --- Application State ---
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private User currentUser;
    private IDataService dataService;
    private JPanel feedContainer;
    private JLabel statusLabel;
    private AutoRefreshTask refreshThread;

    // UI Vars for Read Panel
    private JLabel readTitleLabel;
    private JLabel readAuthorLabel;
    private JLabel readAvatarLabel; // NEW: Avatar for read screen
    private JTextArea readContentArea;
    private JLabel readLikesLabel;
    private DefaultListModel<String> commentsListModel;
    private BlogPost currentViewingPost;

    public SimpleBlogApp() {
        // 1. Initialize Logic
        dataService = new DatabaseService();

        // 2. Initialize Dummy Data (if needed)
        if (dataService.getAllPosts().isEmpty()) {
            User demo = new RegularUser("DemoUser");
            MemoryStore.posts.add(new BlogPost("Welcome", "Database not connected. Using Memory Mode.", demo));
        }

        // 3. UI Setup
        setTitle("Java Blog System");
        setSize(550, 800); // Slightly wider for avatars
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 4. Create Screens
        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createFeedPanel(), "Feed");
        mainPanel.add(createWritePostPanel(), "Write");
        mainPanel.add(createReadPostPanel(), "Read");

        // 5. Status Bar & Threading
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Status: Ready");
        statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        refreshThread = new AutoRefreshTask(statusLabel);
        refreshThread.start();

        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    // --- NEW: Dynamic Avatar Generator ---
    // Draws a circle with the user's initial. No external image files needed!
    private ImageIcon generateAvatar(String username, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // Enable smooth edges (Antialiasing)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Pick a consistent color based on the username
        int hash = username.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;
        // Ensure color isn't too light (so white text shows up)
        Color avatarColor = new Color((r + 50) % 200, (g + 50) % 200, (b + 50) % 200);

        // 2. Draw Circle
        g2.setColor(avatarColor);
        g2.fillOval(0, 0, size, size);

        // 3. Draw Initial
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, size / 2));

        String initial = username.substring(0, 1).toUpperCase();
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(initial)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();

        g2.drawString(initial, x, y);
        g2.dispose();

        return new ImageIcon(img);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Blog System Login");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JTextField userField = new JTextField(15);
        userField.setBorder(BorderFactory.createTitledBorder("Username"));
        JPasswordField passField = new JPasswordField(15);
        passField.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(24, 156, 14));
        loginBtn.setForeground(Color.WHITE);

        loginBtn.addActionListener(e -> {
            String uName = userField.getText();
            String pass = new String(passField.getPassword());

            if (dataService.authenticateUser(uName, pass)) {
                if (uName.equalsIgnoreCase("admin")) {
                    currentUser = new AdminUser(uName);
                } else {
                    currentUser = new RegularUser(uName);
                }
                JOptionPane.showMessageDialog(this, currentUser.getWelcomeMessage());
                refreshFeed();
                cardLayout.show(mainPanel, "Feed");
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; panel.add(title, gbc);
        gbc.gridy = 1; panel.add(userField, gbc);
        gbc.gridy = 2; panel.add(passField, gbc);
        gbc.gridy = 3; panel.add(loginBtn, gbc);
        return panel;
    }

    private JPanel createFeedPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        header.setBackground(new Color(147, 144, 144));

        JLabel title = new JLabel(" Recent Posts");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setIcon(new ImageIcon()); // Placeholder for an app icon if you wanted one

        JButton writeBtn = new JButton("Write Post");
        JButton logoutBtn = new JButton("Logout");

        writeBtn.addActionListener(e -> cardLayout.show(mainPanel, "Write"));
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "Login"));

        JPanel btns = new JPanel();
        btns.setOpaque(false);
        btns.add(writeBtn);
        btns.add(logoutBtn);

        header.add(title, BorderLayout.WEST);
        header.add(btns, BorderLayout.EAST);

        feedContainer = new JPanel();
        feedContainer.setLayout(new BoxLayout(feedContainer, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(feedContainer);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void refreshFeed() {
        feedContainer.removeAll();
        List<BlogPost> posts = dataService.getAllPosts();

        for (BlogPost post : posts) {
            // Create Main Card
            JPanel card = new JPanel(new BorderLayout(10, 10)); // Gap of 10px
            card.setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(5, 10, 5, 10),
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true) // Rounded look
            ));
            card.setBackground(Color.WHITE);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

            // --- LEFT: Avatar ---
            JLabel avatarLbl = new JLabel(generateAvatar(post.author.getUsername(), 50));

            // --- CENTER: Title & Meta ---
            JLabel pTitle = new JLabel(post.title);
            pTitle.setFont(new Font("Arial", Font.BOLD, 16));

            JLabel pMeta = new JLabel("By " + post.author.getUsername());
            pMeta.setFont(new Font("Arial", Font.PLAIN, 12));
            pMeta.setForeground(Color.GRAY);

            JPanel info = new JPanel(new GridLayout(2, 1));
            info.setOpaque(false);
            info.add(pTitle);
            info.add(pMeta);

            // --- RIGHT: Read Button ---
            JButton readBtn = new JButton("Read >");
            readBtn.addActionListener(e -> openReadPanel(post));

            // Add to Card
            card.add(avatarLbl, BorderLayout.WEST);
            card.add(info, BorderLayout.CENTER);
            card.add(readBtn, BorderLayout.EAST);

            feedContainer.add(card);
            feedContainer.add(Box.createVerticalStrut(10));
        }
        feedContainer.revalidate();
        feedContainer.repaint();
    }

    private JPanel createWritePostPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField titleField = new JTextField();
        titleField.setBorder(BorderFactory.createTitledBorder("Post Title"));

        JTextArea contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Write your story..."));

        JButton pubBtn = new JButton("Publish Post");
        JButton cancelBtn = new JButton("Cancel");

        cancelBtn.addActionListener(e -> cardLayout.show(mainPanel, "Feed"));

        pubBtn.addActionListener(e -> {
            try {
                String t = titleField.getText();
                String c = contentArea.getText();
                if (t.isEmpty() || c.isEmpty()) throw new Exception("Fields cannot be empty");

                BlogPost newPost = new BlogPost(t, c, currentUser);
                dataService.addPost(newPost);

                titleField.setText("");
                contentArea.setText("");
                refreshFeed();
                cardLayout.show(mainPanel, "Feed");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        JPanel btns = new JPanel();
        btns.add(cancelBtn);
        btns.add(pubBtn);

        panel.add(titleField, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createReadPostPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBorder(new EmptyBorder(10, 20, 10, 20));

        JButton back = new JButton("<< Back");
        back.addActionListener(e -> cardLayout.show(mainPanel, "Feed"));

        topContainer.add(back, BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Initialize Labels
        readAvatarLabel = new JLabel();
        readAvatarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        readTitleLabel = new JLabel("Title");
        readTitleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        readTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        readAuthorLabel = new JLabel("Author");
        readAuthorLabel.setForeground(Color.GRAY);
        readAuthorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        readContentArea = new JTextArea(10, 30);
        readContentArea.setEditable(false);
        readContentArea.setLineWrap(true);
        readContentArea.setWrapStyleWord(true);
        readContentArea.setOpaque(false);
        readContentArea.setFont(new Font("Georgia", Font.PLAIN, 16));

        // Helper panel to hold avatar + author info side by side
        JPanel authorHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        authorHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        authorHeader.add(readAvatarLabel);
        authorHeader.add(readAuthorLabel);

        readLikesLabel = new JLabel("Likes: 0");
        JButton likeBtn = new JButton("Like This Post");
        likeBtn.addActionListener(e -> {
            if(currentViewingPost != null) {
                currentViewingPost.likes++;
                readLikesLabel.setText("Likes: " + currentViewingPost.likes);
            }
        });
        JPanel likePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        likePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        likePanel.add(likeBtn);
        likePanel.add(readLikesLabel);

        // Add elements to scrollable content
        content.add(readTitleLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(authorHeader); // Avatar + Name
        content.add(Box.createVerticalStrut(20));
        content.add(readContentArea);
        content.add(Box.createVerticalStrut(20));
        content.add(likePanel);

        JPanel commentsPanel = new JPanel(new BorderLayout());
        commentsPanel.setBorder(BorderFactory.createTitledBorder("Comments"));
        commentsListModel = new DefaultListModel<>();

        JPanel addComPanel = new JPanel(new BorderLayout());
        JTextField comField = new JTextField();
        JButton comBtn = new JButton("Post Comment");

        comBtn.addActionListener(e -> {
            String txt = comField.getText();
            if(!txt.isEmpty()) {
                String comment = currentUser.getUsername() + ": " + txt;
                currentViewingPost.comments.add(comment);
                commentsListModel.addElement(comment);
                comField.setText("");
            }
        });

        addComPanel.add(comField, BorderLayout.CENTER);
        addComPanel.add(comBtn, BorderLayout.EAST);

        commentsPanel.add(new JScrollPane(new JList<>(commentsListModel)), BorderLayout.CENTER);
        commentsPanel.add(addComPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(content), commentsPanel);
        split.setDividerLocation(450);

        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void openReadPanel(BlogPost post) {
        this.currentViewingPost = post;

        readTitleLabel.setText(post.title);
        readAuthorLabel.setText("Written by " + post.author.getUsername());
        readContentArea.setText(post.content);
        readLikesLabel.setText("Likes: " + post.likes);

        // Generate larger avatar for reading view
        readAvatarLabel.setIcon(generateAvatar(post.author.getUsername(), 60));

        commentsListModel.clear();
        for(String c : post.comments) commentsListModel.addElement(c);

        cardLayout.show(mainPanel, "Read");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimpleBlogApp().setVisible(true);
        });
    }
}