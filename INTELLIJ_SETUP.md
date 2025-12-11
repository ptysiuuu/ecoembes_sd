# IntelliJ IDEA Configuration Guide

## Opening the Projects

### Method 1: All Projects in One Window (Recommended)

1. Open IntelliJ IDEA
2. **File → Open**
3. Navigate to the folder containing all 4 projects
4. Select the folder `Ecoembes-Separated`
5. Click **OK**
6. IntelliJ will detect multiple Gradle projects
7. Wait for Gradle sync to complete for all projects

### Method 2: Individual Project Windows

1. **File → Open**
2. Select `ecoembes-server` folder
3. Click **OK**
4. Repeat steps 1-3 for:
   - `plassb-server`
   - `contsocket-server`
   - `webclient`

## Creating a Compound Run Configuration

This allows you to start all 4 services with ONE button click!

### Step 1: Create Individual Run Configurations

IntelliJ should auto-detect these, but if not:

1. **Run → Edit Configurations**
2. Click **"+"** → **Gradle**
3. Name: `ecoembes-server [bootRun]`
4. Gradle project: select `ecoembes-server`
5. Tasks: `bootRun`
6. Click **OK**

Repeat for:
- `plassb-server [bootRun]` with task `bootRun`
- `contsocket-server [run]` with task `run`
- `webclient [bootRun]` with task `bootRun`

### Step 2: Create Compound Configuration

1. **Run → Edit Configurations**
2. Click **"+"** → **Compound**
3. Name: `Run All Ecoembes Services`
4. Click **"+"** under "Run configurations"
5. Add in this order:
   - `ecoembes-server [bootRun]`
   - `plassb-server [bootRun]`
   - `contsocket-server [run]`
   - `webclient [bootRun]`
6. Click **Apply** → **OK**

### Step 3: Run All Services

1. Select "Run All Ecoembes Services" from the run configuration dropdown
2. Click the **green Run button** ▶️
3. All 4 services will start in parallel!

## Verifying Setup

After running, check the Run tool window (Alt+4). You should see 4 tabs:
- `ecoembes-server [bootRun]` - Port 8081
- `plassb-server [bootRun]` - Port 8083  
- `contsocket-server [run]` - Port 9090
- `webclient [bootRun]` - Port 8082

## Service URLs

- **Ecoembes API**: http://localhost:8081/swagger-ui.html
- **Web Client**: http://localhost:8082
- **PlasSB API**: http://localhost:8083

## Troubleshooting

### Gradle Sync Issues
- **File → Invalidate Caches / Restart**
- Right-click on project → **Gradle → Reload Gradle Project**

### Port Already in Use
- Stop all services: **Run → Stop All** (Ctrl+F2)
- Or use `stop-all-services.ps1` script

### Java Version Issues
- **File → Project Structure → Project**
- Set SDK to Java 21
- Set Language Level to 21

### Cannot Find Main Class
- Make sure Gradle build completed successfully
- Check **Build → Rebuild Project**

## Tips

- Use the **Services** tool window (Alt+8) for better visualization
- Enable **"Auto-Restart"** in Spring Boot run configuration for hot reload
- Use **Ctrl+F2** to stop all running configurations
- Check console logs in the **Run** tool window for startup messages

## Next Steps

Once all services are running:
1. Open browser: http://localhost:8082
2. Login with: `admin@ecomebes.com` / `admin123`
3. Test the complete workflow!

